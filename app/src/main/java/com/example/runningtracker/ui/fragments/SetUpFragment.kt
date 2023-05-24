package com.example.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentSetupBinding
import com.example.runningtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtracker.utils.Constants.KEY_NAME
import com.example.runningtracker.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetUpFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject /// because this is a primitive data type
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(layoutInflater)

        if (!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setUpFragment , true)
                .build()

            findNavController().navigate(
                R.id.action_setUpFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }


        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_setUpFragment_to_runFragment)
            } else {
                Snackbar.make(requireView() , "please enter all the fields" , Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }


    private fun writePersonalDataToSharedPref(): Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty() ){
            return false
        }

        /// saving value in shared preferences
        sharedPref.edit()
            .putString(KEY_NAME , name)
            .putFloat(KEY_WEIGHT , weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE , false)
            .apply()

        val toolBarText = "Let,s go, $name"
        requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolBarText

        return true
    }


}