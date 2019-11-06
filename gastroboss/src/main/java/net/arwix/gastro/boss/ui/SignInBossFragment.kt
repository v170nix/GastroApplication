package net.arwix.gastro.boss.ui


import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.android.synthetic.main.fragment_sign_in_boss.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mjson.Json
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.extension.weak
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.data.GoogleCloudPrintApi
import net.arwix.gastro.boss.data.auth.AccessTokenProvider
import net.arwix.gastro.boss.data.auth.CloudPrintInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * A simple [Fragment] subclass.
 */
class SignInBossFragment : Fragment(), CoroutineScope by MainScope() {

    private val accessTokenProvider: AccessTokenProvider by inject()
    private val googlePrintApi: GoogleCloudPrintApi by inject()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
//    private lateinit var gso: GoogleSignInOptions
//    private lateinit var gsi: GoogleSignInClient
    private var retryError = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in_boss, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.liveState.observe(this,  Observer(::renderProfile))
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        Log.e("render state", state.toString())
        if (state.account != null) {
            findNavController(this).navigate(R.id.summaryBossFragment)
            profile_state_login.visible()
            profile_state_logout.gone()
        } else {
            profile_state_login.gone()
            profile_state_logout.visible()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profile_state_login.gone()
        profile_state_logout.visible()
//        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.app_client_id))
//            .requestEmail()
//            .requestServerAuthCode(getString(R.string.app_client_id))
//            .requestScopes(Scope("https://www.googleapis.com/auth/cloudprint"))
//            .build()
//
//        gsi = GoogleSignIn.getClient(requireActivity(), gso)

        start_button.setOnClickListener {
//            getPrinters("3424")
            profileViewModel.nonCancelableIntent(ProfileViewModel.Action.LoginStart(this.weak()))
//            launch {
//                getPrs()
//            }
//            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
//            if (account == null) {
//                signIn()
//            } else {
//                Log.e("account", account.serverAuthCode.toString())
//                Log.e("account", account.idToken.toString())
//                Log.e("account", account.isExpired.toString())
//                if (account.isExpired) {
//                    gsi.signOut()
//                } else {
//                    gsi.signOut()
//                }
//            }
        }
        stop_button.setOnClickListener {
            val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (account != null) {
                Log.e("account", account.serverAuthCode.toString())
                Log.e("account", account.idToken.toString())
                Log.e("account", account.isExpired.toString())
                Log.e("acc.displ", account.displayName.toString())
                Log.e("acc.displ", account.email.toString())
                Log.e("acc.displ", account.photoUrl.toString())
//                if (account.isExpired) {
//                    gsi.signOut()
//                } else {
//                    gsi.signOut()
//                }
            }
//            gsi.silentSignIn()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        profileViewModel.nonCancelableIntent(ProfileViewModel.Action.LoginDoActivityResult(requestCode, data))
//        if (requestCode != RC_SIGN_IN) return
//        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//        try {
//            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
//            launch {
//                runCatching {
//                    accessTokenProvider.updateToken(account!!.serverAuthCode)
//                }.onFailure {
//                    Log.e("throw onactivity", it.message.toString())
//                }.onSuccess {
//                    Log.e("sus", it.toString())
//                }
//                getPrs()
//            }
////            firebaseAuthWithGoogle(account!!)
//        } catch (e: ApiException) {
//            Log.e("Google sign in failed", e.toString())
//        }
    }

    private fun signIn() {
//        val t = gsi.silentSignIn()
//        val ac = t.getResult(ApiException::class.java)!!
//        startActivityForResult(gsi.signInIntent, RC_SIGN_IN)
    }

    private suspend fun getPrs() {
        val token = accessTokenProvider.getAccessToken()
        if (token == null) {
            signIn()
        } else {
            runCatching {
                googlePrintApi.getPrinters("Bearer $token")
            }.onSuccess {
                Log.e("prs sus", "1")
                val printer = it.printers?.firstOrNull() ?: return
                Log.e("printer", printer.toString())
//                googlePrintApi.submitPrintJob("Bearer $token",
//                    it.xsrfToken!!,
//                    printer.id!!,
//                    "title1",
//                    getTicket(),
//                    "test",
//                    "text/plain"
//                )


                val context = resources.openRawResource(R.raw.electricity).readBytes()
                val b64Context = Base64.encodeToString(context, Base64.DEFAULT)

//                val requestBody =
//                    b64Context.toRequestBody()

//                val mpart = MultipartBody.Builder()
//                    .addFormDataPart("xsrf", it.xsrfToken!!)
//                    .addFormDataPart("printerid", printer.id!!)
//                    .addFormDataPart("title", "title2")
//                    .addFormDataPart("ticket", getTicket())
//                    .addFormDataPart("content", b64Context)
//                    .addFormDataPart("contentType", "application/pdf")
//                    .build()
//
//                val me = googlePrintApi.testSub("Bearer $token", mpart)
//
//                Log.e("res", me.toString())

                val responseBody = googlePrintApi.submitFilePrintJob("Bearer $token",
                    "base64",
                    it.xsrfToken!!,
                    printer.id!!,
                    "title1",
                    getTicket(),
                    b64Context,
                    "application/pdf"
                )
                Log.e("res", responseBody.toString())

                val responseBody1 = googlePrintApi.submitFilePrintJob("Bearer $token",
                    "base64",
                    it.xsrfToken!!,
                    printer.id!!,
                    "title2",
                    getTicket(),
                    b64Context,
                    "application/pdf"
                )
                Log.e("res", responseBody1.toString())


            }.onFailure {
                Log.e("prs failure", it.message.toString())
                if (it is HttpException) {
                    retryError++
                    accessTokenProvider.updateToken()
                    if (retryError < 2) {
                        getPrs()
                    }
                }
            }
            Log.e("2", "2")
        }
    }

    private fun getPrinters(token: String) {
//        val credential = GoogleAuthProvider.getCredential(token, null)
        val req = Request.Builder()
            .url("https://www.google.com/cloudprint/submit")
            .addHeader("Authorization", "Bearer $token")
            .build()
        val client = OkHttpClient()

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val clientI = okhttp3.OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(CloudPrintInterceptor())
            .build()


        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.google.com/cloudprint/")
            .client(clientI)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

        val retrofit1 = Retrofit.Builder()
            .baseUrl("https://www.google.com/cloudprint/")
            .client(clientI)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val api = retrofit.create(GoogleCloudPrintApi::class.java)
        val api1 = retrofit1.create(GoogleCloudPrintApi::class.java)

        launch(Dispatchers.IO) {

//            val response = client.newCall(req).execute()
//            val r = api.getPrinters("Bearer $token")
//            val printer = r.printers?.firstOrNull() ?: return@launch
//            Log.e("printer", printer.id.toString())
            val ticket = getTicket()


//            withContext(Dispatchers.Main)
//            {
//                Ion.with(this@SignInBossFragment)
//                    .load("POST", "https://www.google.com/cloudprint/submit")
//                    .addHeader("Authorization", "Bearer $token")
//                    .setMultipartParameter("printerid", printer.id!!)
//                    .setMultipartParameter("title", "print test")
//                    .setMultipartParameter("ticket", getTicket())
////                .setMultipartFile("content", "application/pdf", new File(pdfPath))
//                    .asString()
//                    .withResponse()
//                    .setCallback { e, result ->
//                        if (e == null) {
//                            Log.e("result", result.result)
//                        } else {
//                            Log.e("result e", e.toString())
//                        }
//                    }
//
//
//            }



//            val ticketBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ticket)
//
//            val b = resources.openRawResource(R.raw.electricity).readBytes().toString()
////                .let {
////                    Base64.encodeToString(it, Base64.DEFAULT)
////                }
//            //    .toRequestBody("application/pdf".toMediaType())
//
//            val cont = "test body".let { Base64.encodeToString(it.toByteArray(), Base64.DEFAULT) }.toRequestBody()
//
//            val multipartBody = MultipartBody.Part.createFormData("context", null, cont)

//            api1.submitPrintJob("Bearer $token",
//                r.xsrfToken!!,
//                printer.id!!,
//                "title",
//                ticket,
//                b,
//                "application/pdf"
//                )

//            api1.submitPrintJob("Bearer $token",
////                r.xsrfToken!!,
//                "printer.id!!",
//                "title1",
//                ticket,
//                "test",
//                "text/plain"
//            )

            //Failed to parse the print job's print ticket.



//            val json = JSONObject(response.body().string())
//            Log.e("result", json.toString(5))
        }

    }

    private fun getTicket(): String
    {
        val ticket = Json.`object`()
        val print = Json.`object`()
        ticket.set("version", "1.0")
        print.set("vendor_ticket_item", Json.array())
        print.set("color", Json.`object`("type", "STANDARD_MONOCHROME"))
        print.set("copies", Json.`object`("copies", 1));
        ticket.set("print", print)
        return ticket.toString()
    }

    private companion object {
        const val RC_SIGN_IN = 372
    }
}
