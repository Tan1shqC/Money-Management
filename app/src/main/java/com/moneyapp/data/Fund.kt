import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "funds")
data class Fund(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val monthlyAllocation: Long,
    val currentBalance: Long = 0L,
    val carryForwardEnabled: Boolean = true,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Notes
 * 
 * In a way fund can be considered a secondary entity
 * If we have a transaction of 20k every month we can link it to event and a category
 * and fund will only be the balance of that category.
 * And for inter fund transfer we can create two events for each fund (debit and credit) and
 * link it to a 0 transaction, sort of elegant.
 * 
 * But then again there are other events that will be added later because of their automated nature
 * and so on that will get an event created and linked during weekly audit. And when finally when 
 * all transactions have been successfully linked we can mark that as a checkpoint and update the fund. 
 * But then when calculating fund balance we would have mark events as resolved and only include other
 * events.
 */