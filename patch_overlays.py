import re

with open("app/src/main/java/com/example/ui/Overlays.kt", "r") as f:
    content = f.read()

old_overlay = """@Composable
fun ResultOverlay(
    isWin: Boolean,
    totalGems: Int,
    onAction: (GameAction) -> Unit
) {"""

new_overlay = """import android.app.Activity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.viewmodel.GameViewModel
import com.example.data.GemDataStore
import com.example.ads.RewardedAdManager

@Composable
fun ResultOverlay(
    isWin: Boolean,
    totalGems: Int,
    roundId: String,
    gameViewModel: GameViewModel,
    gemDataStore: GemDataStore,
    rewardedAdManager: RewardedAdManager,
    onAction: (GameAction) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = androidx.compose.runtime.rememberCoroutineScope()
"""

content = content.replace(old_overlay, new_overlay)

with open("app/src/main/java/com/example/ui/Overlays.kt", "w") as f:
    f.write(content)

