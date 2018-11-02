package catt.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import catt.custom.view.DropDragBubbleView
import catt.custom.view.OnDropDragBubbleListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val _TAG: String by lazy { MainActivity::class.java.simpleName }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DropDragBubbleView.attach(button, object : OnDropDragBubbleListener {
            override fun onDismiss(view: View) {
                Log.e(_TAG, "onDismiss: ")
                Toasty.warning(applicationContext, "Dismiss Button.", Toast.LENGTH_SHORT, false).show()
            }

            override fun onSpringBack(view: View) {
                Log.e(_TAG, "onSpringBack: ")
                Toasty.info(applicationContext, "Spring Back Button.", Toast.LENGTH_SHORT, false).show()
            }
        })

        DropDragBubbleView.attach(image_view, object : OnDropDragBubbleListener {
            override fun onDismiss(view: View) {
                Log.e(_TAG, "onDismiss: ")
                Toasty.warning(applicationContext, "Dismiss ImageView.", Toast.LENGTH_SHORT, false).show()
            }

            override fun onSpringBack(view: View) {
                Log.e(_TAG, "onSpringBack: ")
                Toasty.info(applicationContext, "Spring Back ImageView.", Toast.LENGTH_SHORT, false).show()
            }
        })
    }

    override fun onDestroy() {
        this.clearFindViewByIdCache()
        super.onDestroy()
    }
}
