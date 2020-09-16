package com.automizely.mvp.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.automizely.mvp.databinding.LayoutActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private val viewBinding: LayoutActivityHomeBinding by lazy {
        LayoutActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

}