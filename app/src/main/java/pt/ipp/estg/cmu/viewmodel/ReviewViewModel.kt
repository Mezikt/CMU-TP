package pt.ipp.estg.cmu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.Review
import pt.ipp.estg.cmu.data.ReviewRepository
import com.google.firebase.firestore.ktx.firestore

// UI State for the Review Screen
data class ReviewUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val reviews: List<Review> = emptyList()
)

class ReviewViewModel(private val reviewRepository: ReviewRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    val auth = Firebase.auth
    val firestore = Firebase.firestore


    fun submitReview(pointId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _uiState.value = ReviewUiState(isLoading = true)

            val userId = Firebase.auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = ReviewUiState(errorMessage = "User not authenticated.")
                return@launch
            }

            if (rating == 0) {
                 _uiState.value = ReviewUiState(errorMessage = "Please provide a rating.")
                return@launch
            }

            val review = Review(
                pointId = pointId,
                userId = userId,
                rating = rating,
                comment = comment
            )

            val result = reviewRepository.saveReview(review)

            result.onSuccess {
                _uiState.value = ReviewUiState(isSuccess = true)
            }.onFailure {
                _uiState.value = ReviewUiState(errorMessage = "Failed to submit review: ${it.message}")
            }
        }
    }

    fun loadReviews(){
        viewModelScope.launch {
            _uiState.value = ReviewUiState(isLoading = true)
            try {
                val reviews = reviewRepository.getReviewsFromUser()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reviews = reviews,
                    isSuccess = true,
                    errorMessage = null
                )

            } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Failed to load data: ${e.message}"
            )
        }
}
    }

    // Factory for manual instantiation
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ReviewRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
                return ReviewViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
