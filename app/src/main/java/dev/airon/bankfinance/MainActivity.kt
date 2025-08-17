package dev.airon.bankfinance

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Linka o menu com os destinos
        bottomNavigation.setupWithNavController(navController)



        // Esconde a bottom bar em telas de autenticação
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val shouldShowBottomBar = when (destination.id) {
                R.id.homeFragment,
                R.id.rechargeFragment,
                R.id.extractFragment,
                R.id.transferFragment,
                R.id.profileFragment -> true

                else -> false
            }

            bottomNavigation.visibility = if (shouldShowBottomBar) View.VISIBLE else View.GONE
        }
    }
}

