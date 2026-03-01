package com.moneyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.moneyapp.data.MoneyDatabase
import com.moneyapp.ui.AppShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = MoneyDatabase.getDatabase(this)
        
        setContent {
            MaterialTheme {
                AppShell(database)
            }
        }
    }
}
