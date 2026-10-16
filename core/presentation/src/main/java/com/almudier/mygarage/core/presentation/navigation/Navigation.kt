package com.almudier.mygarage.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface NavigationCommand {
    data class Navigate(val key: NavKey, val singleTop: Boolean = false, val popUpToInclusive: NavKey? = null) : NavigationCommand
    data object Back : NavigationCommand
    data class ReplaceAll(val key: NavKey) : NavigationCommand
}

interface Navigator {
    val commands: Flow<NavigationCommand>
    fun navigate(key: NavKey, singleTop: Boolean = false, popUpToInclusive: NavKey? = null)
    fun back()
    fun replaceAll(key: NavKey)
}

@Singleton
class AppNavigator @Inject constructor() : Navigator {
    private val channel = Channel<NavigationCommand>(Channel.BUFFERED)
    override val commands = channel.receiveAsFlow()
    override fun navigate(key: NavKey, singleTop: Boolean, popUpToInclusive: NavKey?) {
        channel.trySend(NavigationCommand.Navigate(key, singleTop, popUpToInclusive))
    }
    override fun back() { channel.trySend(NavigationCommand.Back) }
    override fun replaceAll(key: NavKey) { channel.trySend(NavigationCommand.ReplaceAll(key)) }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigatorModule {
    @Binds @Singleton abstract fun bindNavigator(impl: AppNavigator): Navigator
}
