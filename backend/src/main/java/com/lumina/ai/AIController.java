package com.lumina.ai;
import com.lumina.common.Result;
import com.lumina.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 智能导购")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @Operation(summary = "AI 对话")
    @PostMapping("/chat")
    public Result<ChatDTO.Response> chat(@RequestBody ChatDTO.Request req) {
        Long userId = UserContext.getUserId();
        return Result.success(aiService.chat(req.getMessage(), req.getHistory(), userId));
    }

}
