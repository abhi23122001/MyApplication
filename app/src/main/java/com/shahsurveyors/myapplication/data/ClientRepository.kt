package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shahsurveyors.myapplication.models.ClientModel
import com.shahsurveyors.myapplication.models.ClientPaymentRecord
import kotlinx.coroutines.tasks.await

class ClientRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val clientsCollection = firestore.collection(FirebaseConstants.COLLECTION_CLIENTS)
    private val paymentsCollection = firestore.collection("client_payment_history")

    suspend fun getAllClients(): List<ClientModel> = try { clientsCollection.get().await().toObjects(ClientModel::class.java) } catch (_: Exception) { emptyList() }
    suspend fun saveClient(client: ClientModel) { val id=client.id.ifBlank{clientsCollection.document().id}; clientsCollection.document(id).set(client.copy(id=id),SetOptions.merge()).await() }

    suspend fun addReceivable(clientId:String,amount:Double){require(amount>0){"Enter a valid receivable amount"};firestore.runTransaction{tx->val ref=clientsCollection.document(clientId);val snap=tx.get(ref);val current=snap.getDouble("totalReceivable")?:0.0;val now=Timestamp.now();val uid=FirebaseAuth.getInstance().currentUser?.uid?:"ADMIN";val name=FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank{"Admin"}?:"Admin";val historyRef=paymentsCollection.document();tx.update(ref,"totalReceivable",current+amount);tx.set(historyRef,mapOf("id" to historyRef.id,"clientId" to clientId,"clientName" to (snap.getString("name")?:""),"amount" to amount,"paymentDate" to now,"paymentMode" to "DUE ADDED","referenceNumber" to "","remark" to "Amount due added","recordedByUid" to uid,"recordedByName" to name,"createdAt" to now,"type" to "RECEIVABLE"))}.await()}

    suspend fun recordPayment(client:ClientModel,amount:Double,paymentDate:Timestamp,mode:String,reference:String,remark:String){require(amount>0){"Enter a valid payment amount"};firestore.runTransaction{tx->val clientRef=clientsCollection.document(client.id);val snap=tx.get(clientRef);val receivable=snap.getDouble("totalReceivable")?:client.totalReceivable;val paid=snap.getDouble("totalPaid")?:client.totalPaid;val pending=(receivable-paid).coerceAtLeast(0.0);require(amount<=pending+0.01){"Payment cannot be greater than pending amount ₹${String.format("%.2f",pending)}"};val paymentRef=paymentsCollection.document();val uid=FirebaseAuth.getInstance().currentUser?.uid?:"ADMIN";val name=FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank{"Admin"}?:"Admin";val now=Timestamp.now();tx.set(paymentRef,mapOf("id" to paymentRef.id,"clientId" to client.id,"clientName" to client.name,"amount" to amount,"paymentDate" to paymentDate,"paymentMode" to mode,"referenceNumber" to reference.trim(),"remark" to remark.trim(),"recordedByUid" to uid,"recordedByName" to name,"createdAt" to now,"type" to "PAYMENT"));tx.update(clientRef,"totalPaid",paid+amount)}.await()}

    suspend fun getPaymentHistory(clientId:String):List<ClientPaymentRecord> = try { paymentsCollection.whereEqualTo("clientId",clientId).get().await().documents.map{d->ClientPaymentRecord(id=d.id,clientId=d.getString("clientId")?:"",clientName=d.getString("clientName")?:"",amount=d.getDouble("amount")?:0.0,paymentDate=d.getTimestamp("paymentDate"),paymentMode=d.getString("paymentMode")?:"",referenceNumber=d.getString("referenceNumber")?:"",remark=d.getString("remark")?:"",recordedByUid=d.getString("recordedByUid")?:"",recordedByName=d.getString("recordedByName")?:"",createdAt=d.getTimestamp("createdAt"),type=d.getString("type")?:"PAYMENT")}.sortedByDescending{it.paymentDate?.toDate()?.time?:0L} } catch(_:Exception){emptyList()}
}
