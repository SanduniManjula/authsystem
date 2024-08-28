package authsystem.model;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class RoleSearchCriteria {
    private String name;
    private Boolean activated;
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return PageRequest.of(page, size, direction, sortBy);
    }
}
