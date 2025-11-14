package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PerfilPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = Icons.Default.Person,
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rafael",
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "rafael@email.com",
            style = MaterialTheme.typography.body2
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { /* TODO: Implementar logout */ }) {
            Text("Logout")
        }
    }
}


@Composable
fun Login(modifier: Modifier = Modifier){
    var isLoggedIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    TextField(label={Text("Email")}, value=email, onValueChange = {email=it})
    TextField(label={Text("Password")}, value=password, onValueChange = {password=it})


    LaunchedEffect(true){
        scope.launch{
            try {
                val auth= Firebase.auth
                val result = auth
                    .createUserWithEmailAndPassword("teste1@gmail.com", "12345678").await()
                    //.signInWithEmailAndPassword("teste1@gmail.com", "12345678").await()

                if(result!=null && result.user?.email.equals("teste1@gmail.com"))
                {
                    isLoggedIn=true
                }
            }catch(e:Exception){

            }


        }
    }

    Text("Is Logged in : $isLoggedIn")
}

@Composable
fun FireStoreComponent(modifier:Modifier){
    val scope = rememberCoroutineScope()
    var dbText by remember{mutableStateOf("")}

    LaunchedEffect(true) {
        scope.launch {
//            val db = Firebase.firestore
//        val obj = db.collection("profiles").document("").get().await()
//        dbText = obj.data.toString()


            val db = Firebase.firestore
            val obj = db.collection("profiles").document("1aF7SacMOPkp7BKLSQjG").get().await()

        }
    }
}