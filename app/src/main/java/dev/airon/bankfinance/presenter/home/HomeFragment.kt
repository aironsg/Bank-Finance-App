package dev.airon.bankfinance.presenter.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomNavigationView: BottomNavigationView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomNavigation()
    }

    private fun  initBottomNavigation(){
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.navigation_home ->{
                    true
                }

                R.id.navigation_charge_phone -> {

                }

                R.id.navigation_extract -> {

                }

                R.id.navigation_transfer -> {

                }

                R.id.navigation_profile -> {

                }

                else -> false
            } as Boolean

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}