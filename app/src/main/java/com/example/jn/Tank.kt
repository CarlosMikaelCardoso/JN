package com.example.jn

import com.google.firebase.firestore.DocumentId

data class Tank(
    @DocumentId
    val id: String = "",
    val name: String = ""
    // A lista de batidas foi removida daqui!
)