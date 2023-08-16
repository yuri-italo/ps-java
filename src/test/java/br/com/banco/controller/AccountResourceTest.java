package br.com.banco.controller;

import br.com.banco.dto.AccountDto;
import br.com.banco.entity.Transference;
import br.com.banco.entity.Type;
import br.com.banco.repository.AccountRepository;
import br.com.banco.repository.TransferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountResourceTest {

    public static final String URL = "/api/accounts/";
    public static final String ACCOUNT_DTO_NAME = "Joseph Smith";
    private static final String SECOND_ACCOUNT_DTO_NAME = "Carlos Maia";
    public static final int FIRST_ID = 1;
    public static final int SECOND_ID = 2;
    private static final int NON_EXISTING_ID = Integer.MAX_VALUE;
    public static final String EMPTY_NAME = "";
    public static final String LONG_NAME =
            "ThisIsAVeryLongNameThatExceedsFiftyCharactersAndShouldCauseValidationErrors";
    public static final String VALIDATION_ERROR_TITLE = "Validation Error";
    private static final String BUSINESS_ERROR_TITLE = "Business error";
    private static final String CONSTRAINT_VIOLATION_ERROR_TITLE = "Constraint violation error";
    public static final String METHOD_ARGUMENT_NOT_VALID_EXCEPTION_MSG =
            "org.springframework.web.bind.MethodArgumentNotValidException";
    private static final String BUSINESS_EXCEPTION_MSG = "br.com.banco.exception.BusinessException";
    private static final String DATA_INTEGRITY_VIOLATION_EXCEPTION_MSG =
            "org.springframework.dao.DataIntegrityViolationException";
    public static final String MISSING_OWNER_NAME_MSG =
            "Missing Owner Name. Please provide the name of the owner to proceed with the request.";
    public static final String TOO_LONG_NAME_MSG =
            "Owner Name must not exceed 50 characters in length.";
    private static final String INVALID_ID_MSG =
            "Invalid ID. The specified ID does not exist in our records. Please check and try again.";


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferenceRepository transferenceRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void save_ValidData_ShouldCreateAnAccountAndReturn201Status() throws Exception {
        // given
        var accountDto = buildAccountDto();
        var valueAsString = objectMapper.writeValueAsString(accountDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(FIRST_ID));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.ownerName").value(ACCOUNT_DTO_NAME));
        resultActions.andExpect(MockMvcResultMatchers.header().string("Location", URL + FIRST_ID));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void save_EmptyOwnerName_ShouldNotCreateAnAccountAndReturn400Status() throws Exception {
        // given
        var accountDto = buildEmptyNamedAccountDto();
        var valueAsString = objectMapper.writeValueAsString(accountDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(VALIDATION_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.BAD_REQUEST.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value(METHOD_ARGUMENT_NOT_VALID_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.ownerName")
                .value(MISSING_OWNER_NAME_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void save_MaxSizeOwnerNameExceeded_ShouldNotCreateAnAccountAndReturn400Status() throws Exception {
        // given
        var accountDto = buildTooBigNamedAccountDto();
        var valueAsString = objectMapper.writeValueAsString(accountDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(VALIDATION_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.BAD_REQUEST.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value(METHOD_ARGUMENT_NOT_VALID_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.ownerName")
                .value(TOO_LONG_NAME_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void findById_ExistingId_ShouldFindMatchingAccountAndReturn200Status() throws Exception {
        // given
        var entity = buildAccountDto().toEntity();
        accountRepository.save(entity);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL + FIRST_ID)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(FIRST_ID));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.ownerName").value(ACCOUNT_DTO_NAME));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void findById_NonExistingId_ShouldNotFindAccountAndReturn404Status() throws Exception {
        // given
        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL + NON_EXISTING_ID)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(BUSINESS_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.NOT_FOUND
                .value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception").value(BUSINESS_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.IllegalArgumentException")
                .value(INVALID_ID_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void findAll_PersistedAccountsPresent_ShouldGetAllAccountsAndReturn200Status() throws Exception {
        // given
        var entity = buildAccountDto().toEntity();
        var entity2 = buildSecondAccountDto().toEntity();
        accountRepository.save(entity);
        accountRepository.save(entity2);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[*]").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[*].id")
                .value(Matchers.containsInAnyOrder(FIRST_ID, SECOND_ID)));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[*].ownerName")
                .value(Matchers.containsInAnyOrder(ACCOUNT_DTO_NAME, SECOND_ACCOUNT_DTO_NAME)));
    }

    @Test
    void findAll_NonPersistedAccounts_ShouldGetEmptyListAndReturn200Status() throws Exception {
        // given
        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[*]").isEmpty());
    }

    @Test
    void update_ExistingAccount_ShouldUpdateAccountAndReturn200Status() throws Exception {
        // given
        var account = buildAccountDto().toEntity();
        accountRepository.save(account);
        var accountDto = buildSecondAccountDto();
        var valueAsString = objectMapper.writeValueAsString(accountDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.put(URL + FIRST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(FIRST_ID));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.ownerName").value(SECOND_ACCOUNT_DTO_NAME));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void update_NonExistingAccount_ShouldNotUpdateAccountAndReturn404Status() throws Exception {
        // given
        var accountDto = buildSecondAccountDto();
        var valueAsString = objectMapper.writeValueAsString(accountDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.put(URL + NON_EXISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(BUSINESS_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.NOT_FOUND.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception").value(BUSINESS_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.IllegalArgumentException")
                .value(INVALID_ID_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void delete_ExistingAccount_ShouldDeleteAccountAndReturn204Status() throws Exception {
        // given
        var account = buildSecondAccountDto().toEntity();
        accountRepository.save(account);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.delete(URL + FIRST_ID)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isNoContent());
        resultActions.andExpect(MockMvcResultMatchers.content().string(Matchers.blankOrNullString()));
    }

    @Test
    void delete_NonExistingAccount_ShouldReturn404Status() throws Exception {
        // given
        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.delete(URL + NON_EXISTING_ID)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(BUSINESS_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.NOT_FOUND.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception").value(BUSINESS_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.IllegalArgumentException")
                .value(INVALID_ID_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void delete_ReferencedAccount_ShouldReturn409Status() throws Exception {
        // given
        var account = buildAccountDto().toEntity();
        accountRepository.save(account);
        transferenceRepository.save(new Transference(
                100.00,
                Type.TRANSFERENCE,
                account,
                SECOND_ACCOUNT_DTO_NAME));

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.delete(URL + FIRST_ID)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isConflict());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title")
                .value(CONSTRAINT_VIOLATION_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.CONFLICT.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value(DATA_INTEGRITY_VIOLATION_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.ConstraintViolationException")
                .isNotEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    static AccountDto buildAccountDto() {
        return new AccountDto(ACCOUNT_DTO_NAME);
    }
    static AccountDto buildSecondAccountDto() {
        return new AccountDto(SECOND_ACCOUNT_DTO_NAME);
    }

    private AccountDto buildEmptyNamedAccountDto() {
        return new AccountDto(EMPTY_NAME);
    }

    private AccountDto buildTooBigNamedAccountDto() {
        return new AccountDto(LONG_NAME);
    }
}
