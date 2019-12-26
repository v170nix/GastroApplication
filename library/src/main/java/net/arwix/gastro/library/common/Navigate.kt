package net.arwix.gastro.library.common

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.findNavController

fun NavDirections.navigate(fragment: Fragment) {
    findNavController(fragment).navigate(this)
}