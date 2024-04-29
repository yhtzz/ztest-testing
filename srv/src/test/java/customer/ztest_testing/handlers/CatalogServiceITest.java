package customer.ztest_testing.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
public class CatalogServiceITest {
    private static final String booksURI = "/api/browse/Books";
    private static final String ADMIN_USER_STRING = "admin";
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(ADMIN_USER_STRING)
    public void discountApplied() throws Exception {
        ResultActions resctions = mockMvc.perform(get(booksURI + "?$filter=stock gt 200&top=1"));
        resctions.andExpect(status().isOk())
                .andExpect(jsonPath("$.value[0].title").value(containsString("11% discount")));
        // 打印结果
        System.out.println("http code = " + resctions.andReturn().getResponse().getStatus());
        System.out.println("http body = " + resctions.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(ADMIN_USER_STRING)
    public void discountNotApplied() throws Exception {
        ResultActions resctions = mockMvc.perform(get(booksURI + "?$filter=stock lt 100&top=1"));
        resctions.andExpect(status().isOk())
                .andExpect(jsonPath("$.value[0].title").value(not(containsString("11% discount"))));

        // 打印结果
        System.out.println("http code = " + resctions.andReturn().getResponse().getStatus());
        System.out.println("http body = " + resctions.andReturn().getResponse().getContentAsString());

    }
}
