package com.example.nattklar.view.wikiscreen

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * @return an index on a navigations position based on the NavHost
 */
fun intArgument(key: String): NamedNavArgument {
    return navArgument(key) {
        type = NavType.IntType
    }
}