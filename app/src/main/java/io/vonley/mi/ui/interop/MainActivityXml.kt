package io.vonley.mi.ui.interop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.R
import io.vonley.mi.databinding.ActivityMainXmlBinding

@AndroidEntryPoint
class MainActivityXml : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private val navController
        get() = navHostFragment.navController

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun onDestinationChanged(binding: ActivityMainXmlBinding): (NavController, NavDestination, Bundle?) -> Unit {
        return { controller, destination, arguments ->
            val i = when (destination.id) {
                R.id.fragment_payload -> {
                    binding.fab.setOnClickListener(null)
                    binding.fab.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivityXml,
                            R.drawable.icon_svg_upload_two
                        )
                    )
                    View.VISIBLE
                }

                R.id.fragment_ftp -> {
                    binding.fab.setOnClickListener(null)
                    binding.fab.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivityXml,
                            R.drawable.ic_svg_upload
                        )
                    )
                    View.VISIBLE
                }

                R.id.fragment_console -> {
                    binding.fab.setOnClickListener(null)
                    binding.fab.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivityXml,
                            R.drawable.icon_svg_add
                        )
                    )
                    View.VISIBLE
                }

                R.id.fragment_home -> {
                    binding.fab.setOnClickListener(null)
                    binding.fab.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivityXml,
                            R.drawable.icon_svg_info
                        )
                    )
                    View.VISIBLE
                }

                else -> View.GONE
            }
            binding.fab.visibility = i
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreviewHomeFragmentView() { binding ->
                this.navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
                setSupportActionBar(binding.bottomAppBar)
                NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
                navController.addOnDestinationChangedListener(onDestinationChanged(binding))
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
fun PreviewHomeFragmentView(binding: ((binding: ActivityMainXmlBinding) -> Unit)? = null) {
    AndroidViewBinding(ActivityMainXmlBinding::inflate) {
        binding?.invoke(this)
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewHomeFragmentView2() {
    AndroidView(factory = { ctx ->
        val inflate = LayoutInflater.from(ctx).inflate(R.layout.activity_main_xml, null)
        inflate
    }) {

    }
}