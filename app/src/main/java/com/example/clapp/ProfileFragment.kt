package com.example.clapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val loginButton = view.findViewById<Button>(R.id.button_login)
        loginButton.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }

        val registerButton = view.findViewById<Button>(R.id.button_register)
        registerButton.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterActivity::class.java))
        }

        val faceScanButton = view.findViewById<Button>(R.id.button_face_scan)
        faceScanButton.setOnClickListener {
            val intent = Intent(requireActivity(), FaceScanActivity::class.java)
            intent.putExtra("maxFaces", 50)
            startActivity(intent)
        }

        return view
    }
}
