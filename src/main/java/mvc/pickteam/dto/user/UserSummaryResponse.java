package mvc.pickteam.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {
    
    private Long id;
    private String name;
    private String profileImage;
    private String role;
} 