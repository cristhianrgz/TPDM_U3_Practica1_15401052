package mx.edu.tpdm_u3_practica1_15401052

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var descripcion : EditText ?= null
    var monto : EditText ?= null
    var fechaVen : EditText ?= null
    //var pagado : EditText ?= null
    var listaView : ListView ?= null
    var insertar : Button ?= null
    var actualizar : Button ?= null
    var radio1 : CheckBox ?= null
    var varPagado = ""
    //declarando el objeto firestore
    var baseRemota = FirebaseFirestore.getInstance()

    //declarar objetos tipo arreglo dinamico
    var registrosRemotos = ArrayList<String>()
    var keys = java.util.ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        descripcion = findViewById(R.id.editDescripcion)
        monto = findViewById(R.id.editMonto)
        fechaVen = findViewById(R.id.editfecha)
        listaView = findViewById(R.id.listaRegitrados)
        insertar = findViewById(R.id.btnInsertar)
        actualizar = findViewById(R.id.btnActualizar)
        radio1 = findViewById(R.id.radio1)


        insertar?.setOnClickListener {
            if(radio1?.isChecked == true){
                Toast.makeText(this,"Elegiste SI", Toast.LENGTH_SHORT).show()
                varPagado = "true"
            }
            else{
                varPagado = "false"
            }

            var insertarDatos = hashMapOf(
                "descripcion" to descripcion?.text.toString(),
                "monto" to monto?.text.toString().toDouble(),
                "fechaVencimiento" to fechaVen?.text.toString(),
                "pagado" to varPagado
                //"pagado" to pagado?.text.toString()
            )

            baseRemota.collection("recibopagos").add(insertarDatos as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this,"Se inserto correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this,"No se pudo insertar", Toast.LENGTH_SHORT).show()
                }
            limpiarCampos()
        }
        baseRemota.collection("recibopagos").addSnapshotListener { querySnapshot, e ->
            if(e != null){
                Toast.makeText(this,"Error, No se pudo realizar la consulta", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            registrosRemotos.clear()
            keys.clear()

            for (document in querySnapshot!!){
                var cadena = document.getString("descripcion")+" -- "+document.getDouble("monto")+" -- "+
                        document.getString("fechaVencimiento")+"\n"
                registrosRemotos.add(cadena)
                keys.add(document.id)
            }

            if(registrosRemotos.size == 0){
                registrosRemotos.add("NO HAY REGISTROS PARA MOSTRAR")
            }

            var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, registrosRemotos)
            listaView?.adapter = adapter
        }
        listaView?.setOnItemClickListener { adapterView, view, i, l ->
            if(keys.size == 0){
                return@setOnItemClickListener
            }
            AlertDialog.Builder(this).setTitle("ATENCION").setMessage("¿Que deseas hacer con "+registrosRemotos.get(i)+" ?")
                .setPositiveButton("Eliminar"){dialog, which ->
                    baseRemota.collection("recibopagos").document(keys.get(i)).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this,"Se elimino correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            Toast.makeText(this,"Error, No se pudo eliminar correctamente", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Actualizar"){dialog, which ->
                    baseRemota.collection("recibopagos").document(keys.get(i)).get()

                        .addOnSuccessListener {
                            descripcion?.setText(it.getString("descripcion"))
                            monto?.setText(it.getDouble("monto").toString())
                            fechaVen?.setText(it.getString("fechaVencimiento"))
                            var pagadoSN = it.getBoolean("pagado").toString()
                            if(pagadoSN == "true"){
                                radio1?.setChecked(true)
                            }
                            else radio1?.setChecked(false)
                            //pagado?.setText(it.getString("pagado"))
                        }
                        .addOnFailureListener{
                            descripcion?.setText("NO SE ENCONTRO DATO")
                            monto?.setText("NO SE ENCONTRO DATO")
                            fechaVen?.setText("NO SE ENCONTRO DATO")
                            //pagado?.setText("NO SE ENCONTRO DATO")

                            descripcion?.isEnabled = false
                            monto?.isEnabled = false
                            fechaVen?.isEnabled = false
                            //pagado?.isEnabled = false
                            actualizar?.isEnabled = false
                        }

                    actualizar?.setOnClickListener{

                        var datosActualizar = hashMapOf(
                            "descripcion" to descripcion?.text.toString(),
                            "monto" to monto?.text.toString().toDouble(),
                            "fechaVencimiento" to fechaVen?.text.toString(),
                            "pagado" to radio1?.isChecked()

                            //"pagado" to pagado?.text.toString()
                        )
                        baseRemota.collection("recibopagos").document(keys.get(i)).set(datosActualizar as Map<String, Any>)
                            .addOnSuccessListener {
                                limpiarCampos()
                                Toast.makeText(this,"Se actualizo correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener{
                                Toast.makeText(this,"Error, No se pudo actualizar correctamente", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNeutralButton("Cancelar"){dialog, which -> }.show()
        }
    }

    fun limpiarCampos(){
        descripcion?.setText("")
        monto?.setText("")
        fechaVen?.setText("")
        radio1?.setChecked(false)
        //pagado?.setText("")
    }
}
