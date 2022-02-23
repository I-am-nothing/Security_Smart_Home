package com.sakun.security_home.fragmentPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.sakun.security_home.R
import com.sakun.security_home.databinding.FragmentFastLoginPageBinding
import com.sakun.security_home.databinding.FragmentModePageBinding

class Mode_page : Fragment() {

    private var _binding: FragmentModePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentModePageBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.outDoorModeBtn.setOnClickListener{
            messageBox("Need Permission", "When you press yes and allow permissions, your phone camera can take capture camera images.")
        }

        return view
    }

    private fun messageBox(title: String, message: String){
        val alertDialog = view?.let {
            AlertDialog.Builder(it.context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.mipmap.ic_launcher)
                .setNegativeButton("No"){ _, _ ->
                }
                .setPositiveButton("Yes"){ _, _ ->
                    view?.let{
                        Navigation.findNavController(it).navigate(R.id.action_mode_page_to_outDoorMode_page)
                    }
                }
        }
        alertDialog?.show()
    }
}