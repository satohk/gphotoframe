package com.satohk.gphotoframe.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.satohk.gphotoframe.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class MenuBarItem(
    val itemType: MenuBarItemType,
    val action: SideBarAction,
    val album: Album? = null,
) {
    enum class MenuBarItemType{
        SHOW_ALL,
        SHOW_PHOTO,
        SHOW_MOVIE,
        SHOW_ALBUM_LIST,
        SEARCH,
        SETTING,
        ALBUM_ITEM
    }
}

class MenuBarViewModel(
    private val _accountState: AccountState
    ) : SideBarActionPublisherViewModel() {
    private val _itemList = MutableStateFlow(listOf<MenuBarItem>())
    val itemList: StateFlow<List<MenuBarItem>> get() = _itemList

    var lastFocusIndex: Int = 0
        private set

    fun initItemList(sideBarType: SideBarType) {
        _itemList.value = listOf()
        viewModelScope.launch {
            when (sideBarType) {
                SideBarType.TOP -> {
                    _itemList.emit(
                        listOf(
                            MenuBarItem(
                                MenuBarItem.MenuBarItemType.SHOW_ALL,
                                SideBarAction(SideBarActionType.ENTER_GRID,
                                            gridContents=GridContents())
                            ),
                            MenuBarItem(
                                MenuBarItem.MenuBarItemType.SHOW_PHOTO,
                                SideBarAction(SideBarActionType.ENTER_GRID,
                                    gridContents=GridContents(searchQuery=SearchQuery(mediaType=MediaType.PHOTO)))
                            ),
                            MenuBarItem(
                                MenuBarItem.MenuBarItemType.SHOW_MOVIE,
                                SideBarAction(SideBarActionType.ENTER_GRID,
                                    gridContents=GridContents(searchQuery=SearchQuery(mediaType=MediaType.VIDEO)))
                            ),
                            MenuBarItem(
                                MenuBarItem.MenuBarItemType.SHOW_ALBUM_LIST,
                                SideBarAction(SideBarActionType.CHANGE_SIDEBAR,
                                    sideBarType=SideBarType.ALBUM_LIST)
                            ),
                            MenuBarItem(
                                MenuBarItem.MenuBarItemType.SEARCH,
                                SideBarAction(SideBarActionType.CHANGE_SIDEBAR,
                                    sideBarType=SideBarType.SEARCH)
                            ),
                            MenuBarItem(
                                MenuBarItem.MenuBarItemType.SETTING,
                                SideBarAction(SideBarActionType.CHANGE_SIDEBAR,
                                    sideBarType=SideBarType.SETTING)
                            ),
                        )
                    )
                }
                SideBarType.ALBUM_LIST -> {
                    if (_accountState.photoRepository.value != null) {
                        val albumList = _accountState.photoRepository.value!!.getAlbumList()

                        _itemList.emit(
                            albumList.map { album ->
                                MenuBarItem(
                                    MenuBarItem.MenuBarItemType.ALBUM_ITEM,
                                    SideBarAction(SideBarActionType.ENTER_GRID,
                                        gridContents=GridContents(searchQuery = SearchQuery(album=album))),
                                    album=album
                                )
                            }
                        )
                    }
                    else{
                        Log.d("loadNextImageList", "_accountState.photoRepository.value is null")
                    }
                }
            }
        }
    }

    fun enterToGrid(itemIndex: Int) {
        publishAction(_itemList.value[itemIndex].action)
    }

    fun goBack(){
        val action = SideBarAction(
            SideBarActionType.BACK,
            gridContents = null
        )
        publishAction(action)
    }

    fun changeFocus(itemIndex: Int) {
        lastFocusIndex = itemIndex

        val action = _itemList.value[itemIndex].action
        if(action.actionType == SideBarActionType.ENTER_GRID) {
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            val focusAction = SideBarAction(
                SideBarActionType.CHANGE_GRID,
                gridContents=action.gridContents
            )
            publishAction(focusAction)
        }
    }

    fun loadIcon(menuBarItem: MenuBarItem, width:Int?, height:Int?, callback:(bmp:Bitmap?)->Unit) {
        if(_accountState.photoRepository.value != null) {
            viewModelScope.launch {
                if (_accountState.photoRepository.value != null && menuBarItem.album != null) {
                    val bmp = _accountState.photoRepository.value!!.getAlbumCoverPhoto(
                        menuBarItem.album,
                        width,
                        height,
                        true
                    )
                    callback.invoke(bmp)
                }
            }
        }
    }
}
