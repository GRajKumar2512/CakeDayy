package com.pocketaps.cakeday.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.pocketaps.cakeday.feature.editperson.navigation.EditPersonRoute
import com.pocketaps.cakeday.feature.editperson.navigation.editPersonScreen
import com.pocketaps.cakeday.feature.groups.navigation.GroupsRoute
import com.pocketaps.cakeday.feature.groups.navigation.groupsScreen
import com.pocketaps.cakeday.feature.people.contactsimport.navigation.ImportContactsRoute
import com.pocketaps.cakeday.feature.people.contactsimport.navigation.importContactsScreen
import com.pocketaps.cakeday.feature.people.navigation.PeopleRoute
import com.pocketaps.cakeday.feature.people.navigation.peopleScreen
import com.pocketaps.cakeday.feature.settings.navigation.SettingsRoute
import com.pocketaps.cakeday.feature.settings.navigation.settingsScreen

@Composable
fun CakeDayyNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = PeopleRoute) {
        peopleScreen(
            onAddPerson = { navController.navigate(EditPersonRoute()) },
            onEditPerson = { personId -> navController.navigate(EditPersonRoute(personId = personId)) },
            onOpenSettings = { navController.navigate(SettingsRoute) },
            onOpenGroups = { navController.navigate(GroupsRoute) },
            onOpenImportContacts = { navController.navigate(ImportContactsRoute) },
        )
        editPersonScreen(
            onNavigateBack = { navController.popBackStack() },
        )
        settingsScreen(
            onNavigateBack = { navController.popBackStack() },
        )
        groupsScreen(
            onNavigateBack = { navController.popBackStack() },
        )
        importContactsScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
