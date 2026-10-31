package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.IptvAccount
import com.example.localization.LocalKrasStrings
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.GlassWhiteStroke
import com.example.ui.theme.KrasBlack
import com.example.ui.theme.KrasRed
import com.example.ui.theme.KrasRedBright
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.LiveRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagerDialog(
    activeAccount: IptvAccount?,
    allAccounts: List<IptvAccount>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSwitchAccount: (IptvAccount) -> Unit,
    onDeleteAccount: (IptvAccount) -> Unit,
    onAddM3u: (name: String, url: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onAddXtream: (name: String, server: String, user: String, pass: String, onResult: (Boolean, String?) -> Unit) -> Unit
) {
    val strings = LocalKrasStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Accounts List, 1 = Add M3U, 2 = Add Xtream

    // Form inputs
    var accountName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF50D1016),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.iptvAccounts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab bar: Accounts, Add M3U, Add Xtream
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = KrasRedBright,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = KrasRedBright
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; errorMessage = null },
                    text = {
                        Text(
                            text = strings.manageAccounts,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) TextPrimary else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; errorMessage = null },
                    text = {
                        Text(
                            text = strings.typeM3u,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) TextPrimary else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; errorMessage = null },
                    text = {
                        Text(
                            text = strings.typeXtream,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) TextPrimary else TextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LiveRed.copy(alpha = 0.2f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = LiveRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (selectedTab) {
                0 -> {
                    // Accounts List & Demo Mode
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Demo Mode Account Card
                        item {
                            val isDemoActive = activeAccount?.type == AccountType.DEMO
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(16.dp),
                                        backgroundColor = if (isDemoActive) Color(0x33E50914) else Color(0x221A1E27),
                                        borderColor = if (isDemoActive) KrasRed else GlassWhiteStroke.copy(alpha = 0.2f)
                                    )
                                    .clickable {
                                        onSwitchAccount(
                                            IptvAccount(
                                                id = -1,
                                                name = "KRAS TV Demo Mode",
                                                type = AccountType.DEMO,
                                                isActive = true
                                            )
                                        )
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleOutline,
                                        contentDescription = null,
                                        tint = KrasRedBright,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = strings.useDemoMode,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = strings.demoModeNotice,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 2
                                        )
                                    }

                                    if (isDemoActive) {
                                        GlassBadge(
                                            text = strings.activeStatus,
                                            backgroundColor = KrasRed
                                        )
                                    }
                                }
                            }
                        }

                        // User saved accounts
                        items(allAccounts) { account ->
                            val isActive = activeAccount?.id == account.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(16.dp),
                                        backgroundColor = if (isActive) Color(0x33E50914) else Color(0x221A1E27),
                                        borderColor = if (isActive) KrasRed else GlassWhiteStroke.copy(alpha = 0.2f)
                                    )
                                    .clickable { onSwitchAccount(account) }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = account.name,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            GlassBadge(
                                                text = account.type.name,
                                                backgroundColor = Color(0x44FFFFFF)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${account.channelsCount} Channels • ${account.moviesCount} Movies • ${account.seriesCount} Series",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isActive) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = strings.activeStatus,
                                                tint = LiveGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        IconButton(onClick = { onDeleteAccount(account) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = strings.deleteAccount,
                                                tint = LiveRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Add M3U Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = accountName,
                            onValueChange = { accountName = it },
                            label = { Text(strings.accountName) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KrasRed,
                                unfocusedBorderColor = GlassWhiteStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = m3uUrl,
                            onValueChange = { m3uUrl = it },
                            label = { Text(strings.playlistUrl) },
                            placeholder = { Text("https://example.com/playlist.m3u8") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KrasRed,
                                unfocusedBorderColor = GlassWhiteStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (m3uUrl.isBlank()) {
                                    errorMessage = strings.fillAllFields
                                    return@Button
                                }
                                isSubmitting = true
                                errorMessage = null
                                onAddM3u(accountName, m3uUrl) { success, err ->
                                    isSubmitting = false
                                    if (success) {
                                        selectedTab = 0
                                        accountName = ""
                                        m3uUrl = ""
                                    } else {
                                        errorMessage = err ?: strings.invalidUrl
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KrasRed,
                                contentColor = Color.White
                            ),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = strings.connectAndSave,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Add Xtream Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = accountName,
                            onValueChange = { accountName = it },
                            label = { Text(strings.accountName) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KrasRed,
                                unfocusedBorderColor = GlassWhiteStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = xtreamServer,
                            onValueChange = { xtreamServer = it },
                            label = { Text(strings.serverUrl) },
                            placeholder = { Text("http://xtream-server.com:8080") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KrasRed,
                                unfocusedBorderColor = GlassWhiteStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = xtreamUser,
                            onValueChange = { xtreamUser = it },
                            label = { Text(strings.username) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KrasRed,
                                unfocusedBorderColor = GlassWhiteStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = xtreamPass,
                            onValueChange = { xtreamPass = it },
                            label = { Text(strings.password) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KrasRed,
                                unfocusedBorderColor = GlassWhiteStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (xtreamServer.isBlank() || xtreamUser.isBlank() || xtreamPass.isBlank()) {
                                    errorMessage = strings.fillAllFields
                                    return@Button
                                }
                                isSubmitting = true
                                errorMessage = null
                                onAddXtream(accountName, xtreamServer, xtreamUser, xtreamPass) { success, err ->
                                    isSubmitting = false
                                    if (success) {
                                        selectedTab = 0
                                        accountName = ""
                                        xtreamServer = ""
                                        xtreamUser = ""
                                        xtreamPass = ""
                                    } else {
                                        errorMessage = err ?: strings.playbackError
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KrasRed,
                                contentColor = Color.White
                            ),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = strings.connectAndSave,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
