Package com.offlinemessage.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val pttButton = findViewById<Button>(R.id.pttButton)

        statusText.text = "Offline Message is ready"

        pttButton.setOnClickListener {
            statusText.text = "Voice button pressed"
        }
    }
}
