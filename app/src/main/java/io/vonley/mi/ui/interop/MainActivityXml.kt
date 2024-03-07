package io.vonley.mi.ui.interop

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.R
import io.vonley.mi.databinding.ActivityMainXmlBinding
import java.util.zip.Inflater

@AndroidEntryPoint
class MainActivityXml : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var navHostFragment: NavHostFragment
    private val navController
        get() = navHostFragment.navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreviewHomeFragmentView() { binding ->
                this.navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
                NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
                navController.addOnDestinationChangedListener(this)
            }
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

    }

}


@Composable
@Preview(showBackground = true)
fun PreviewHomeFragmentView(binding: ((binding: ActivityMainXmlBinding) -> Unit)? = null) {
    AndroidViewBinding(ActivityMainXmlBinding::inflate) {
        //this.fragmentContainer
        //val myFragment = fragmentContainer.getFragment<HomeFragmentView>()
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