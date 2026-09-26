package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.engine.GameEngine
import com.example.game.inventory.data.InventoryItemEntity
import com.example.game.inventory.data.ItemCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryDialog(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = engine.inventoryRepository
    val items by repository.allItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalCount by repository.itemCount.collectAsStateWithLifecycle(initialValue = 0)
    val totalQuantity by repository.totalQuantity.collectAsStateWithLifecycle(initialValue = 0)

    var selectedCategory by remember { mutableStateOf(ItemCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Filter items based on category and search text
    val filteredItems = items.filter { item ->
        val matchesCategory = when (selectedCategory) {
            ItemCategory.ALL -> true
            else -> item.category.equals(selectedCategory.name, ignoreCase = true)
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Keep selected item synchronized if updated
    val currentSelected = selectedItem?.let { sel ->
        items.find { it.id == sel.id } ?: items.firstOrNull()
    } ?: filteredItems.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xDD0A0F1D))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("inventory_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00E5FF)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Inventory",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PLAYER INVENTORY",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "Local Room SQLite Database • $totalCount Unique Items (${totalQuantity ?: 0} Total Units)",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("inventory_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Category Filter Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categories = listOf(
                        ItemCategory.ALL to "ALL ITEMS",
                        ItemCategory.CONSUMABLE to "CONSUMABLES",
                        ItemCategory.KEY_ITEM to "KEY ITEMS",
                        ItemCategory.MATERIAL to "MATERIALS",
                        ItemCategory.RELIC to "RELICS",
                        ItemCategory.EQUIPMENT to "GEAR",
                        ItemCategory.TREASURE to "TREASURE"
                    )

                    for ((cat, label) in categories) {
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD4AF37),
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFCBD5E1)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Search Bar & Action Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("inventory_search_field"),
                        placeholder = { Text("Search items or lore...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        )
                    )

                    // Quick seed starter items button if empty
                    if (items.isEmpty()) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.seedStartingInventoryIfEmpty()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Seed Provisions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Toast/Feedback banner
                feedbackMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF064E3B))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = msg,
                            color = Color(0xFFD1FAE5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main Content: Split into Items List (Left/Top) and Detail Pane (Right/Bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column: Items List
                    Card(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
                    ) {
                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (items.isEmpty()) "Your inventory is currently empty." else "No matching items found.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Explore the woods, caves, and ruins to collect potions, crystals, keys, and relics!",
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredItems, key = { it.id }) { item ->
                                    val isSelected = currentSelected?.id == item.id
                                    InventoryItemRow(
                                        item = item,
                                        isSelected = isSelected,
                                        onClick = { selectedItem = item }
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Item Detail & Action Pane
                    Card(
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
                    ) {
                        if (currentSelected != null) {
                            ItemDetailPane(
                                item = currentSelected,
                                engine = engine,
                                onUseItem = {
                                    val success = engine.useInventoryItem(currentSelected.itemKey)
                                    feedbackMessage = if (success) {
                                        "Used ${currentSelected.name}!"
                                    } else {
                                        "Cannot use ${currentSelected.name} right now."
                                    }
                                },
                                onDiscardItem = {
                                    coroutineScope.launch {
                                        repository.consumeOrUseItem(currentSelected.itemKey, 1)
                                        feedbackMessage = "Discarded 1x ${currentSelected.name}."
                                    }
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select an item to view details and lore.",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryItemEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rarityColor = getRarityColor(item.rarity)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF161F30))
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = if (isSelected) rarityColor else Color(0xFF243247),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("inventory_item_${item.itemKey}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Item Icon Glyph
                ItemIconGlyph(item = item, sizeDp = 26)

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.rarity,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = rarityColor
                        )
                        Text(
                            text = "•",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = item.category,
                            fontSize = 9.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Quantity Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "x${item.quantity}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
fun ItemDetailPane(
    item: InventoryItemEntity,
    engine: GameEngine,
    onUseItem: () -> Unit,
    onDiscardItem: () -> Unit
) {
    val rarityColor = getRarityColor(item.rarity)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Item Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ItemIconGlyph(item = item, sizeDp = 36)
                Column {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(rarityColor.copy(alpha = 0.2f))
                                .border(0.8.dp, rarityColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.rarity,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = rarityColor
                            )
                        }

                        Text(
                            text = item.category,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Value & Stack Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "In Bag: ${item.quantity} units",
                    fontSize = 10.5.sp,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Valuation: ${item.value} 🪙",
                    fontSize = 10.5.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            }

            // Description / Lore
            Text(
                text = "DESCRIPTION & LORE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB74D)
            )
            Text(
                text = item.description,
                fontSize = 11.5.sp,
                color = Color(0xFFCFD8DC),
                lineHeight = 16.sp
            )

            // Special attribute tags
            if (item.isUsable) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3300E676))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Consumable / Usable in Field",
                        fontSize = 10.sp,
                        color = Color(0xFF69F0AE),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (item.isUsable) {
                Button(
                    onClick = onUseItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("use_inventory_item_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "USE / CONSUME", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Discard button (disabled for key quest items and legendary relics)
            val isProtected = item.category == "KEY_ITEM" || item.category == "RELIC"
            if (!isProtected) {
                Button(
                    onClick = onDiscardItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "DISCARD 1", fontSize = 10.5.sp, color = Color(0xFFFCA5A5))
                }
            }
        }
    }
}

@Composable
fun ItemIconGlyph(item: InventoryItemEntity, sizeDp: Int = 28) {
    val rarityColor = getRarityColor(item.rarity)

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0B132B))
            .border(1.dp, rarityColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp * 0.75f).dp)) {
            val w = size.width
            val h = size.height
            when {
                item.itemKey.contains("potion_health") -> {
                    // Red flask
                    val flask = Path().apply {
                        moveTo(w * 0.35f, 0f)
                        lineTo(w * 0.65f, 0f)
                        lineTo(w * 0.65f, h * 0.3f)
                        lineTo(w * 0.9f, h * 0.85f)
                        lineTo(w * 0.1f, h * 0.85f)
                        lineTo(w * 0.35f, h * 0.3f)
                        close()
                    }
                    drawPath(flask, color = Color(0xFFFF5252))
                    drawCircle(Color.White, radius = 1.8f, center = Offset(w * 0.4f, h * 0.55f))
                }
                item.itemKey.contains("potion_swiftness") -> {
                    // Cyan speed flask / bolt
                    val bolt = Path().apply {
                        moveTo(w * 0.6f, 0f)
                        lineTo(w * 0.15f, h * 0.52f)
                        lineTo(w * 0.5f, h * 0.52f)
                        lineTo(w * 0.4f, h)
                        lineTo(w * 0.85f, h * 0.48f)
                        lineTo(w * 0.5f, h * 0.48f)
                        close()
                    }
                    drawPath(bolt, color = Color(0xFF00E5FF))
                }
                item.itemKey.contains("crystal") -> {
                    val diamond = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        lineTo(w, h * 0.5f)
                        lineTo(w * 0.5f, h)
                        lineTo(0f, h * 0.5f)
                        close()
                    }
                    drawPath(diamond, color = Color(0xFF00E5FF))
                }
                item.itemKey.contains("coin") -> {
                    drawCircle(color = Color(0xFFFFD700), radius = w * 0.45f)
                    drawCircle(color = Color(0xFFFF8F00), radius = w * 0.3f, style = Stroke(width = 1.2f))
                }
                item.itemKey.contains("key") -> {
                    drawCircle(color = Color(0xFFFFD700), radius = w * 0.28f, center = Offset(w * 0.35f, h * 0.35f), style = Stroke(width = 2f))
                    drawLine(Color(0xFFFFD700), Offset(w * 0.55f, h * 0.55f), Offset(w * 0.9f, h * 0.9f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFFFD700), Offset(w * 0.75f, h * 0.75f), Offset(w * 0.88f, h * 0.62f), strokeWidth = 2f)
                }
                item.itemKey.contains("heart") -> {
                    val heart = Path().apply {
                        moveTo(w * 0.5f, h * 0.85f)
                        cubicTo(w * 0.1f, h * 0.55f, 0f, h * 0.25f, w * 0.25f, h * 0.1f)
                        cubicTo(w * 0.45f, h * 0.1f, w * 0.5f, h * 0.3f, w * 0.5f, h * 0.3f)
                        cubicTo(w * 0.5f, h * 0.3f, w * 0.55f, h * 0.1f, w * 0.75f, h * 0.1f)
                        cubicTo(w, h * 0.25f, w * 0.9f, h * 0.55f, w * 0.5f, h * 0.85f)
                        close()
                    }
                    drawPath(heart, color = Color(0xFFFF1744))
                }
                item.itemKey.contains("sword") -> {
                    drawLine(Color(0xFFE2E8F0), Offset(w * 0.15f, h * 0.85f), Offset(w * 0.85f, h * 0.15f), strokeWidth = 2.5f)
                    drawLine(Color(0xFFFFD700), Offset(w * 0.2f, h * 0.65f), Offset(w * 0.35f, h * 0.8f), strokeWidth = 2f)
                }
                else -> {
                    // Generic chest / relic badge
                    val box = Path().apply {
                        moveTo(w * 0.2f, h * 0.25f)
                        lineTo(w * 0.8f, h * 0.25f)
                        lineTo(w * 0.8f, h * 0.8f)
                        lineTo(w * 0.2f, h * 0.8f)
                        close()
                    }
                    drawPath(box, color = rarityColor)
                }
            }
        }
    }
}

fun getRarityColor(rarity: String): Color {
    return when (rarity.uppercase()) {
        "LEGENDARY" -> Color(0xFFFFD700)
        "EPIC" -> Color(0xFFC084FC)
        "RARE" -> Color(0xFF38BDF8)
        "UNCOMMON" -> Color(0xFF34D399)
        else -> Color(0xFF94A3B8)
    }
}
