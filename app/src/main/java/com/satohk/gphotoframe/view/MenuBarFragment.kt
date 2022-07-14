package com.satohk.gphotoframe.view

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.satohk.gphotoframe.*

import android.view.View
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satohk.gphotoframe.viewmodel.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


/**
 * Loads a grid of cards with movies to browse.
 */
class MenuBarFragment() : Fragment(R.layout.fragment_menu_bar) {
    private lateinit var _adapter: MenuBarItemAdapter
    private lateinit var _recyclerView: RecyclerView
    private val _viewModel by activityViewModels<MenuBarViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MenuBarFragment", "onViewCreated")

        // set up menubar
        _recyclerView = view.findViewById<RecyclerView>(R.id.menu_bar)
        val numberOfColumns = 1
        _recyclerView.layoutManager =
            GridLayoutManager(requireContext(), numberOfColumns)
        Log.d("MenuBarFragment", this._viewModel.itemList.value.toString())
        _adapter = MenuBarItemAdapter()

        _adapter.onClick = fun(_:View?, position:Int):Unit{
            Log.d("click",  position.toString())
            _viewModel.onClickMenuItem(position)
        }

        _adapter.onFocus = fun(_:View?, position:Int):Unit {
            Log.d("menu onFocus",  position.toString())
            _viewModel.onFocusMenuItem(position)
        }

        _adapter.onKeyDown = fun(_:View?, position:Int, keyEvent: KeyEvent):Boolean{
            Log.d("keydown", keyEvent.toString())
            if(keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                _viewModel.onClickMenuItem(position)
            }
            else if (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                _viewModel.back()
            }
            return false
        }

        _adapter.loadIcon = fun(menuBarItem: MenuBarItem, width:Int?, height:Int?, callback:(bmp: Bitmap?)->Unit) {
            _viewModel.loadIcon(menuBarItem, width, height, callback)
        }

        _recyclerView.adapter = _adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("lifecycleScope", "_viewModel.itemList.collect")
                _viewModel.itemList.collect() {
                    _adapter.setList(_viewModel.itemList.value)
                    _adapter.notifyDataSetChanged()
                    if(_viewModel.itemList.value.size > 0) {
                        setFocus(0)
                        _viewModel.onFocusMenuItem(0)
                    }
                }
            }
        }

    }

    fun restoreLastFocus(){
        setFocus(_viewModel.lastFocusIndex)
    }

    private fun setFocus(index: Int){
        if(_recyclerView != null) {
            val holder = _recyclerView.findViewHolderForAdapterPosition(index)
                    as MenuBarItemAdapter.MenuBarItemViewHolder?
            holder?.binding?.root?.requestFocus()
        }
    }
}