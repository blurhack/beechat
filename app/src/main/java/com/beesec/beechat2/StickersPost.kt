@file:Suppress("CascadeIf")

package com.beesec.beechat2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beesec.beechat2.group.CommentGroupActivity
import com.beesec.beechat2.group.CreateGroupPostActivity
import com.beesec.beechat2.post.CommentActivity
import com.beesec.beechat2.post.CreatePostActivity
import io.stipop.Stipop
import io.stipop.StipopDelegate
import io.stipop.extend.StipopImageView
import io.stipop.model.SPPackage
import io.stipop.model.SPSticker

class StickersPost : AppCompatActivity(), StipopDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stickers)

        val stipopIV = findViewById<StipopImageView>(R.id.stipopIV)

        Stipop.connect(this, stipopIV, "1234", "en", "US", this)

        Stipop.showSearch()

    }

    override fun onStickerSelected(sticker: SPSticker): Boolean {

        if (intent.getStringExtra("activity").equals("post")){
            val i = Intent(this, CreatePostActivity::class.java)
            i.putExtra("gif",  sticker.stickerImg.toString())
            startActivity(i)
            finish()
        }else
        if (intent.getStringExtra("activity").equals("group")){
            val i = Intent(this, CreateGroupPostActivity::class.java)
            i.putExtra("gif",  sticker.stickerImg.toString())
            startActivity(i)
            finish()
        }else
            if (intent.getStringExtra("activity").equals("comment")){
                val i = Intent(this, CommentActivity::class.java)
                i.putExtra("gif",  sticker.stickerImg.toString())
                i.putExtra("postID", intent.getStringExtra("postID"))
                startActivity(i)
                finish()
            }
            else
                if (intent.getStringExtra("activity").equals("groupcomment")){
                    val i = Intent(this, CommentGroupActivity::class.java)
                    i.putExtra("gif",  sticker.stickerImg.toString())
                    i.putExtra("postID", intent.getStringExtra("postID"))
                    i.putExtra("group", intent.getStringExtra("groupId"))
                    startActivity(i)
                    finish()
                }



        return true
    }

    override fun canDownload(spPackage: SPPackage): Boolean {

        return true
    }

}
