package com.beesec.beechat2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.stipop.Stipop
import io.stipop.StipopDelegate
import io.stipop.extend.StipopImageView
import io.stipop.model.SPPackage
import io.stipop.model.SPSticker

class Stickers : AppCompatActivity(), StipopDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stickers)

        val stipopIV = findViewById<StipopImageView>(R.id.stipopIV)

        Stipop.connect(this, stipopIV, "1234", "en", "US", this)

        Stipop.showSearch()

    }

    override fun onStickerSelected(sticker: SPSticker): Boolean {

        val i = Intent(this, SendStickerActivity::class.java)
        i.putExtra("type", intent.getStringExtra("type"))
        i.putExtra("id", intent.getStringExtra("id"))
        i.putExtra("uri",  sticker.stickerImg.toString())
        startActivity(i)
        finish()


        return true
    }

    override fun canDownload(spPackage: SPPackage): Boolean {

        return true
    }

}
