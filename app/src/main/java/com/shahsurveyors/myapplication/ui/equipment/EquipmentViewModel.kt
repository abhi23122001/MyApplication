package com.shahsurveyors.myapplication.ui.equipment

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shahsurveyors.myapplication.data.EquipmentRepository
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.launch

class EquipmentViewModel(private val repository: EquipmentRepository) : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    val equipmentList=mutableStateListOf<EquipmentModel>(); val employees=mutableStateListOf<UserProfile>(); val projects=mutableStateListOf<ProjectModel>()
    fun fetchEquipment(){viewModelScope.launch{isLoading=true;try{equipmentList.clear();equipmentList.addAll(repository.getAllEquipment())}catch(e:Exception){errorMessage=e.localizedMessage?:"Unable to load equipment"}finally{isLoading=false}}}
    fun loadHandoverOptions(){viewModelScope.launch{try{employees.clear();employees.addAll(repository.getActiveEmployees());projects.clear();projects.addAll(repository.getActiveProjects())}catch(e:Exception){errorMessage=e.localizedMessage?:"Unable to load handover options"}}}
    fun handoverEquipment(id:String,employee:UserProfile,project:ProjectModel,location:String,accessories:List<String>,onDone:()->Unit){if(employee.uid.isBlank()){errorMessage="Please select an employee";return};if(project.id.isBlank()){errorMessage="Please select a project";return};if(location.isBlank()){errorMessage="Please enter handover location";return};viewModelScope.launch{isLoading=true;errorMessage=null;try{val admin=FirebaseAuth.getInstance().currentUser;repository.saveHandover(id,employee,project,location,accessories,admin?.uid?:"ADMIN",admin?.displayName?:"Admin");fetchEquipment();onDone()}catch(e:Exception){errorMessage=e.localizedMessage?:"Unable to confirm handover"}finally{isLoading=false}}}
    fun returnEquipment(id:String,location:String,remarks:String){if(location.isBlank()){errorMessage="Please enter return location";return};viewModelScope.launch{isLoading=true;errorMessage=null;try{repository.returnEquipment(id,location,remarks);fetchEquipment()}catch(e:Exception){errorMessage=e.localizedMessage?:"Unable to return equipment"}finally{isLoading=false}}}
    fun updateStatus(id:String,status:String,uid:String?,name:String?){viewModelScope.launch{try{repository.updateEquipmentStatus(id,status,uid,name);fetchEquipment()}catch(e:Exception){errorMessage=e.localizedMessage?:"Unable to update equipment"}}}
    fun clearError(){errorMessage=null}
}
