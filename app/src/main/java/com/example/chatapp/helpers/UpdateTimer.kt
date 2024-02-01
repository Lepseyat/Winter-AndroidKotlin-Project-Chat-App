import android.os.Handler
import com.example.chatapp.GroupChatsHomeScreenActivity

class UpdateTimer(
  private val groupChatsHomeScreenActivity: GroupChatsHomeScreenActivity
  // private val chatActivity: ChatActivity
) {

  private val handler = Handler()

  val updateRunnable: Runnable =
    object : Runnable {
      override fun run() {
        groupChatsHomeScreenActivity.fetchAndUpdateGroupChats()
        // chatActivity.fetchAndUpdateMessages()

        handler.postDelayed(this, 10000)
      }
    }

  fun onDestroy() {
    handler.removeCallbacks(updateRunnable)
  }
}
