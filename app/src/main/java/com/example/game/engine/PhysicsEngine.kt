package com.example.game.engine

import com.example.game.entities.Enemy
import com.example.game.entities.EnemyAIState
import com.example.game.entities.Player
import com.example.game.entities.PlayerState
import com.example.game.model.RectF2D
import com.example.game.model.Vector2D
import com.example.game.world.TileType
import com.example.game.world.WorldMap
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 2D Physics Engine handling character movement, gravity, jumping,
 * and axis-separated collision detection & resolution against solid walls and platforms.
 */
class PhysicsEngine(private val world: WorldMap) {

    companion object {
        const val TERMINAL_FALL_SPEED = 720f
        const val GROUND_ACCEL = 14f
        const val GROUND_DECEL = 18f
        const val AIR_ACCEL = 8.5f
        const val AIR_DECEL = 2.5f
        const val ICE_ACCEL = 3.5f
        const val ICE_DECEL = 1.8f
        const val WALL_SLIDE_MAX_FALL_SPEED = 130f
        const val APEX_GRAVITY_RATIO = 0.55f
        const val COYOTE_TIME_MAX = 0.12f
        const val EPSILON = 0.02f
    }

    /**
     * Updates player physics: ladder climbing, horizontal movement with acceleration/friction,
     * wall collisions, gravity & variable jump dynamics, and vertical platform landing/ceilings.
     */
    fun updatePlayer(
        player: Player,
        inputX: Float,
        inputY: Float,
        dt: Float,
        onHitHazard: () -> Unit,
        onJumpTriggered: (() -> Unit)? = null
    ) {
        if (player.state == PlayerState.DEAD) {
            // Dead player falls with gravity
            player.vel.y = (player.vel.y + player.gravity * dt).coerceAtMost(TERMINAL_FALL_SPEED)
            player.pos.y += player.vel.y * dt
            player.updateBounds()
            return
        }

        // 1. Ladder detection and climbing
        checkLadder(player, inputX, inputY, dt)
        if (player.isClimbing) {
            player.updateBounds()
            return
        }

        // 2. Horizontal Movement (Friction, Acceleration, Max Speed)
        updatePlayerHorizontalMovement(player, inputX, dt)

        // 3. Horizontal Collision Resolution (Solid Walls, Gates, Crumble Walls)
        resolvePlayerHorizontalCollisions(player)

        // 4. Gravity & Vertical Movement
        updatePlayerVerticalPhysics(player, inputX, dt)

        // 5. Vertical Collision Resolution (Platforms, Ground, Ceilings, Moving Platforms)
        val wasGrounded = player.isGrounded
        resolvePlayerVerticalCollisions(player, dt)

        // 6. Coyote Time & Jump Buffering
        if (player.isGrounded) {
            player.coyoteTimer = COYOTE_TIME_MAX
            // If the player buffered a jump just before touching ground, execute it now!
            if (player.jumpBufferTimer > 0f) {
                if (player.jump(inputY)) {
                    onJumpTriggered?.invoke()
                }
            }
        }

        // 7. Hazard spikes check
        if (checkHazardOverlap(player.bounds)) {
            onHitHazard()
        }

        // 8. Keep within world boundaries
        clampToWorldBounds(player)

        player.updateBounds()
    }

    private fun checkLadder(player: Player, inputX: Float, inputY: Float, dt: Float) {
        val feetX = player.pos.x + player.width * 0.5f
        val feetY = player.pos.y + player.height - 4f
        val centerY = player.pos.y + player.height * 0.5f

        val centerTile = world.getTileAtWorldPos(feetX, centerY)
        val feetTile = world.getTileAtWorldPos(feetX, feetY)
        player.isOnLadder = centerTile.isLadder || feetTile.isLadder

        if (player.isOnLadder) {
            if (abs(inputY) > 0.2f) {
                player.isClimbing = true
            }
        } else {
            player.isClimbing = false
        }

        if (player.isClimbing) {
            player.vel.y = inputY * player.effectiveClimbSpeed
            player.vel.x = inputX * (player.effectiveMoveSpeed * 0.6f)
            if (abs(inputX) > 0.1f) {
                player.facingRight = inputX > 0
            }

            player.pos.x += player.vel.x * dt
            resolvePlayerHorizontalCollisions(player)

            val prevY = player.pos.y
            player.pos.y += player.vel.y * dt
            resolvePlayerVerticalCollisions(player, dt, prevY)
        }
    }

    /**
     * Calculates horizontal acceleration and deceleration based on input and surface materials (Ice vs Ground vs Air).
     */
    private fun updatePlayerHorizontalMovement(player: Player, inputX: Float, dt: Float) {
        if (player.state == PlayerState.HURT) {
            // Hurt knockback decay
            player.vel.x *= (1f - 3.5f * dt).coerceIn(0f, 1f)
            player.pos.x += player.vel.x * dt
            return
        }

        val targetVelX = inputX * player.effectiveMoveSpeed
        val isAccelerating = abs(inputX) > 0.05f

        val accelRate = when {
            player.onIce && player.isGrounded -> if (isAccelerating) ICE_ACCEL else ICE_DECEL
            player.isGrounded -> if (isAccelerating) GROUND_ACCEL else GROUND_DECEL
            else -> if (isAccelerating) AIR_ACCEL else AIR_DECEL
        }

        // Smoothly accelerate toward target horizontal velocity
        player.vel.x += (targetVelX - player.vel.x) * (accelRate * dt).coerceAtMost(1f)

        if (abs(inputX) > 0.08f) {
            player.facingRight = inputX > 0
        }

        player.pos.x += player.vel.x * dt
    }

    /**
     * Detects and resolves collisions against solid walls (left and right),
     * preventing penetration and providing step tolerance.
     */
    private fun resolvePlayerHorizontalCollisions(player: Player) {
        val ts = world.tileSize
        val width = player.width
        val height = player.height
        val pos = player.pos
        val vel = player.vel

        player.isTouchingWallLeft = false
        player.isTouchingWallRight = false

        // Vertical span to test against walls (inward vertical inset so feet/head don't catch on floors/ceilings)
        val insetY = 5f
        val startRow = max(0, floor((pos.y + insetY) / ts).toInt())
        val endRow = min(world.rows - 1, floor((pos.y + height - insetY) / ts).toInt())

        if (vel.x > 0) {
            // Moving right: test the leading edge at pos.x + width
            val rightEdgeCol = floor((pos.x + width) / ts).toInt()
            if (rightEdgeCol in 0 until world.cols) {
                var hitWall = false
                for (r in startRow..endRow) {
                    val tile = world.getTile(r, rightEdgeCol)
                    if (tile.isSolid && !tile.isOneWay) {
                        hitWall = true
                        break
                    }
                }
                if (hitWall) {
                    pos.x = rightEdgeCol * ts - width - EPSILON
                    vel.x = 0f
                    player.isTouchingWallRight = true
                }
            }
        } else if (vel.x < 0) {
            // Moving left: test the leading edge at pos.x
            val leftEdgeCol = floor(pos.x / ts).toInt()
            if (leftEdgeCol in 0 until world.cols) {
                var hitWall = false
                for (r in startRow..endRow) {
                    val tile = world.getTile(r, leftEdgeCol)
                    if (tile.isSolid && !tile.isOneWay) {
                        hitWall = true
                        break
                    }
                }
                if (hitWall) {
                    pos.x = (leftEdgeCol + 1) * ts + EPSILON
                    vel.x = 0f
                    player.isTouchingWallLeft = true
                }
            }
        }

        // Check locked gates
        resolveGateHorizontalCollisions(player.bounds, pos, vel, width)

        // Check intact secret crumble walls
        resolveCrumbleWallHorizontalCollisions(player.bounds, pos, vel, width)
    }

    private fun resolveGateHorizontalCollisions(
        bounds: RectF2D,
        pos: Vector2D,
        vel: Vector2D,
        width: Float
    ) {
        for (gate in world.lockedGates) {
            if (gate.isSolid && bounds.overlaps(gate.bounds)) {
                if (vel.x > 0) {
                    pos.x = gate.bounds.left - width - EPSILON
                    vel.x = 0f
                } else if (vel.x < 0) {
                    pos.x = gate.bounds.right + EPSILON
                    vel.x = 0f
                }
            }
        }
    }

    private fun resolveCrumbleWallHorizontalCollisions(
        bounds: RectF2D,
        pos: Vector2D,
        vel: Vector2D,
        width: Float
    ) {
        for (wall in world.crumbleWalls) {
            if (!wall.isBroken && bounds.overlaps(wall.bounds)) {
                if (vel.x > 0) {
                    pos.x = wall.bounds.left - width - EPSILON
                    vel.x = 0f
                } else if (vel.x < 0) {
                    pos.x = wall.bounds.right + EPSILON
                    vel.x = 0f
                }
            }
        }
    }

    /**
     * Applies gravity with terminal velocity, apex hang-time reduction, and wall sliding mechanics.
     */
    private fun updatePlayerVerticalPhysics(player: Player, inputX: Float, dt: Float) {
        // Apex float: reduce gravity slightly near the peak of a jump for a crisp, buoyant arc
        val effectiveGravity = if (abs(player.vel.y) < 45f && !player.isGrounded) {
            player.gravity * APEX_GRAVITY_RATIO
        } else {
            player.gravity
        }

        player.vel.y += effectiveGravity * dt

        // Check for wall sliding: sliding down a wall in midair while pushing toward it
        val isPushingWall = (inputX > 0.2f && player.isTouchingWallRight) ||
                (inputX < -0.2f && player.isTouchingWallLeft)
        if (!player.isGrounded && player.vel.y > 0 && isPushingWall) {
            player.isWallSliding = true
            // Clamp downward fall speed to wall slide velocity
            player.vel.y = player.vel.y.coerceAtMost(WALL_SLIDE_MAX_FALL_SPEED)
        } else {
            player.isWallSliding = false
            player.vel.y = player.vel.y.coerceAtMost(TERMINAL_FALL_SPEED)
        }

        val prevY = player.pos.y
        player.pos.y += player.vel.y * dt
        player.isGrounded = false
        player.onIce = false
    }

    /**
     * Detects and resolves collisions against solid ground platforms, one-way platforms,
     * ceilings, and moving platforms.
     */
    private fun resolvePlayerVerticalCollisions(
        player: Player,
        dt: Float,
        explicitPrevY: Float? = null
    ) {
        val ts = world.tileSize
        val width = player.width
        val height = player.height
        val pos = player.pos
        val vel = player.vel
        val prevY = explicitPrevY ?: (pos.y - vel.y * dt)

        val insetX = 4f
        val startCol = max(0, floor((pos.x + insetX) / ts).toInt())
        val endCol = min(world.cols - 1, floor((pos.x + width - insetX) / ts).toInt())

        if (vel.y >= 0) {
            // 1. Moving downward / Falling: check feet landing on solid platforms or one-way platforms
            val feetY = pos.y + height
            val feetRow = floor(feetY / ts).toInt()

            if (feetRow in 0 until world.rows) {
                for (c in startCol..endCol) {
                    val tile = world.getTile(feetRow, c)
                    val tileTop = feetRow * ts

                    if (tile.isSolid) {
                        if (tile.isOneWay) {
                            // One-way jump-through platform:
                            // Can only land if drop-through timer is inactive AND player was above the platform
                            val dropThroughActive = player.dropThroughTimer > 0f
                            val wasAbove = (prevY + height) <= (tileTop + 8f)
                            if (!dropThroughActive && wasAbove && feetY >= tileTop) {
                                pos.y = tileTop - height
                                vel.y = 0f
                                player.isGrounded = true
                                break
                            }
                        } else {
                            // Standard solid block / platform
                            if (feetY >= tileTop) {
                                pos.y = tileTop - height
                                vel.y = 0f
                                player.isGrounded = true
                                if (tile == TileType.ICE_BLOCK) {
                                    player.onIce = true
                                }
                                break
                            }
                        }
                    }
                }
            }

            // 2. Moving platforms landing and riding
            resolveMovingPlatformCollisions(player, prevY, dt)

        } else {
            // Moving upward / Jumping: check head collision against solid ceilings
            val headY = pos.y
            val headRow = floor(headY / ts).toInt()

            if (headRow in 0 until world.rows) {
                for (c in startCol..endCol) {
                    val tile = world.getTile(headRow, c)
                    // One-way platforms are ignored so character jumps smoothly up through them!
                    if (tile.isSolid && !tile.isOneWay) {
                        val tileBottom = (headRow + 1) * ts
                        pos.y = tileBottom + EPSILON
                        vel.y = 0f
                        break
                    }
                }
            }
        }
    }

    /**
     * Detects moving platform landing, supports riding the platform and inheriting its velocity.
     */
    private fun resolveMovingPlatformCollisions(player: Player, prevY: Float, dt: Float) {
        for (platform in world.movingPlatforms) {
            val platTop = platform.pos.y
            val platLeft = platform.pos.x
            val platRight = platform.pos.x + platform.width

            val playerBottom = player.pos.y + player.height
            val prevPlayerBottom = prevY + player.height

            // Check if player is horizontally overlapping the platform
            val horizontallyOverlaps = (player.pos.x + player.width > platLeft + 2f) &&
                    (player.pos.x < platRight - 2f)

            if (horizontallyOverlaps) {
                // Land on top of platform if coming from above
                if (prevPlayerBottom <= platTop + 8f && playerBottom >= platTop) {
                    player.pos.y = platTop - player.height
                    player.vel.y = 0f
                    player.isGrounded = true

                    // Carry player with moving platform displacement
                    player.pos.x += platform.vel.x * dt
                    player.pos.y += platform.vel.y * dt
                }
            }
        }
    }

    /**
     * Prevents character from moving outside horizontal world boundaries or below map.
     */
    private fun clampToWorldBounds(player: Player) {
        val maxX = world.width - player.width
        if (player.pos.x < 0f) {
            player.pos.x = 0f
            player.vel.x = 0f
        } else if (player.pos.x > maxX) {
            player.pos.x = maxX
            player.vel.x = 0f
        }
    }

    /**
     * Updates enemy physics: applies gravity, moves horizontally, reverses on wall collision,
     * and lands on solid floors/platforms.
     */
    fun updateEnemy(enemy: Enemy, dt: Float) {
        if (enemy.isFlying) {
            enemy.pos.x += enemy.vel.x * dt
            enemy.pos.y += enemy.vel.y * dt
            enemy.updateBounds()
            return
        }

        if (enemy.aiState != EnemyAIState.DEAD) {
            // Apply gravity
            enemy.vel.y = (enemy.vel.y + 900f * dt).coerceAtMost(600f)

            // Horizontal movement and wall collision
            enemy.pos.x += enemy.vel.x * dt
            val collidedX = resolveEnemyHorizontalCollisions(enemy)
            if (collidedX) {
                enemy.facingRight = !enemy.facingRight
                enemy.vel.x = -enemy.vel.x
            }

            // Vertical movement and platform collision
            val prevY = enemy.pos.y
            enemy.pos.y += enemy.vel.y * dt
            enemy.isGrounded = false
            resolveEnemyVerticalCollisions(enemy, prevY)
        }

        enemy.updateBounds()
    }

    private fun resolveEnemyHorizontalCollisions(enemy: Enemy): Boolean {
        val ts = world.tileSize
        val width = enemy.width
        val height = enemy.height
        val pos = enemy.pos
        val vel = enemy.vel
        var collided = false

        val insetY = 4f
        val startRow = max(0, floor((pos.y + insetY) / ts).toInt())
        val endRow = min(world.rows - 1, floor((pos.y + height - insetY) / ts).toInt())

        if (vel.x > 0) {
            val rightCol = floor((pos.x + width) / ts).toInt()
            if (rightCol in 0 until world.cols) {
                for (r in startRow..endRow) {
                    val tile = world.getTile(r, rightCol)
                    if (tile.isSolid && !tile.isOneWay) {
                        pos.x = rightCol * ts - width - EPSILON
                        collided = true
                        break
                    }
                }
            } else if (pos.x + width >= world.width) {
                pos.x = world.width - width - EPSILON
                collided = true
            }
        } else if (vel.x < 0) {
            val leftCol = floor(pos.x / ts).toInt()
            if (leftCol in 0 until world.cols) {
                for (r in startRow..endRow) {
                    val tile = world.getTile(r, leftCol)
                    if (tile.isSolid && !tile.isOneWay) {
                        pos.x = (leftCol + 1) * ts + EPSILON
                        collided = true
                        break
                    }
                }
            } else if (pos.x <= 0f) {
                pos.x = EPSILON
                collided = true
            }
        }

        return collided
    }

    private fun resolveEnemyVerticalCollisions(enemy: Enemy, prevY: Float) {
        val ts = world.tileSize
        val width = enemy.width
        val height = enemy.height
        val pos = enemy.pos
        val vel = enemy.vel

        val insetX = 4f
        val startCol = max(0, floor((pos.x + insetX) / ts).toInt())
        val endCol = min(world.cols - 1, floor((pos.x + width - insetX) / ts).toInt())

        if (vel.y >= 0) {
            val feetY = pos.y + height
            val feetRow = floor(feetY / ts).toInt()

            if (feetRow in 0 until world.rows) {
                for (c in startCol..endCol) {
                    val tile = world.getTile(feetRow, c)
                    val tileTop = feetRow * ts

                    if (tile.isSolid) {
                        if (tile.isOneWay) {
                            val wasAbove = (prevY + height) <= (tileTop + 8f)
                            if (wasAbove && feetY >= tileTop) {
                                pos.y = tileTop - height
                                vel.y = 0f
                                enemy.isGrounded = true
                                break
                            }
                        } else {
                            if (feetY >= tileTop) {
                                pos.y = tileTop - height
                                vel.y = 0f
                                enemy.isGrounded = true
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if given bounds overlap any water or waterfall tiles.
     */
    fun checkWaterOverlap(bounds: RectF2D): Boolean {
        val ts = world.tileSize
        val startCol = max(0, floor(bounds.left / ts).toInt())
        val endCol = min(world.cols - 1, floor(bounds.right / ts).toInt())
        val startRow = max(0, floor(bounds.top / ts).toInt())
        val endRow = min(world.rows - 1, floor(bounds.bottom / ts).toInt())

        for (r in startRow..endRow) {
            for (c in startCol..endCol) {
                val tile = world.getTile(r, c)
                if (tile.isWater || tile == TileType.WATERFALL) {
                    val waterBounds = RectF2D(c * ts, r * ts, (c + 1) * ts, (r + 1) * ts)
                    if (bounds.overlaps(waterBounds)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Checks if given bounds overlap any lethal hazard/spike tiles.
     */
    private fun checkHazardOverlap(bounds: RectF2D): Boolean {
        val ts = world.tileSize
        val startCol = max(0, floor(bounds.left / ts).toInt())
        val endCol = min(world.cols - 1, floor(bounds.right / ts).toInt())
        val startRow = max(0, floor(bounds.top / ts).toInt())
        val endRow = min(world.rows - 1, floor(bounds.bottom / ts).toInt())

        for (r in startRow..endRow) {
            for (c in startCol..endCol) {
                val tile = world.getTile(r, c)
                if (tile.isHazard) {
                    val spikeBounds = RectF2D(c * ts + 4f, r * ts + 10f, (c + 1) * ts - 4f, (r + 1) * ts)
                    if (bounds.overlaps(spikeBounds)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
