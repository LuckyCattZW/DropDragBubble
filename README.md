# DropDragBubble


<img width="320" height="480" src="https://github.com/LuckyCattZW/DropDragBubble/blob/master/gif/demo.gif" />


#### Usage
```kotlin
        DropDragBubbleView.attach(button, object : OnDropDragBubbleListener {
            override fun onDismiss(view: View) {
               //Do something
            }

            override fun onSpringBack(view: View) {
                //Do something
            }
        })

```