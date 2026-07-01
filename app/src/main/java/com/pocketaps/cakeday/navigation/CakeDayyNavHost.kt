package com.pocketaps.cakeday.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.pocketaps.cakeday.feature.editperson.navigation.EditPersonRoute
import com.pocketaps.cakeday.feature.editperson.navigation.editPersonScreen
import com.pocketaps.cakeday.feature.people.navigation.PeopleRoute
import com.pocketaps.cakeday.feature.people.navigation.peopleScreen

@Composable
fun CakeDayyNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = PeopleRoute) {
        peopleScreen(
            onAddPerson = { navController.navigate(EditPersonRoute()) },
            onEditPerson = { personId -> navController.navigate(EditPersonRoute(personId = personId)) },
        )
        editPersonScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
