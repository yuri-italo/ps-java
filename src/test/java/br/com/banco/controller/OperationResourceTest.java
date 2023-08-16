package br.com.banco.controller;

import br.com.banco.dto.DepositDto;
import br.com.banco.dto.TransferenceDto;
import br.com.banco.dto.WithdrawDto;
import br.com.banco.entity.Account;
import br.com.banco.entity.Transference;
import br.com.banco.entity.Type;
import br.com.banco.repository.AccountRepository;
import br.com.banco.repository.TransferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OperationResourceTest {

    private static final String TRANSFERENCE_URL = "/api/operations/transference/";
    private static final String WITHDRAW_URL = "/api/operations/withdraw/";
    private static final String DEPOSIT_URL = "/api/operations/deposit/";
    private static final String BANK_STATEMENT_URL = "/api/operations/bank-statement/";
    public static final double TRANSFER_VALUE = 100d;
    public static final double WITHDRAW_VALUE = 100d;
    public static final double DEPOSIT_VALUE = 100d;
    public static final double INVALID_TRANSFER_VALUE = -1d;
    public static final double INVALID_WITHDRAW_VALUE = Double.MIN_VALUE;
    public static final double INVALID_DEPOSIT_VALUE = Double.MIN_VALUE;
    public static final int INVALID_DESTINATION_ACCOUNT_ID = 0;
    public static final int NON_EXISTING_ACCOUNT_ID = Integer.MAX_VALUE;
    private static final String BUSINESS_ERROR_TITLE = "Business error";
    private static final String SAME_ACCOUNT_ID_ERROR_TITLE = "Same account id";
    public static final String VALIDATION_ERROR_TITLE = "Validation Error";
    public static final String ERROR_CONVERTING_VALUE_TITLE = "Error converting value";
    private static final String BUSINESS_EXCEPTION_MSG = "br.com.banco.exception.BusinessException";
    public static final String SAME_ACCOUNT_ID_EXCEPTION_MSG = "br.com.banco.exception.SameAccountIdException";
    public static final String METHOD_ARGUMENT_NOT_VALID_EXCEPTION_MSG =
            "org.springframework.web.bind.MethodArgumentNotValidException";
    public static final String METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION_MSG =
            "org.springframework.web.method.annotation.MethodArgumentTypeMismatchException";
    private static final String INVALID_ID_MSG =
            "Invalid ID. The specified ID does not exist in our records. Please check and try again.";
    public static final String SAME_ID_MSG = "Source and destination account IDs cannot be the same.";
    public static final String INVALID_TRANSFER_VALUE_MSG = "The 'value' field must be greater than zero(0).";
    public static final String INVALID_DESTINATION_ACCOUNT_ID_MSG =
            "The 'destinationAccountId' field must be equal or greater than one.";
    public static final String VALUE_MUST_NOT_BE_NULL_MSG = "must not be null";
    public static final String INVALID_WITHDRAW_VALUE_MSG = "The 'value' field must be greater than ten(10).";
    public static final String INVALID_DATE = "2020-06-08T04:14:5";
    public static final String NON_EXISTING_TRANSACTION_OPERATOR_NAME = "Non Existing Transaction Operator Name";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferenceRepository transferenceRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void transfer_ValidTransference_ShouldTransferAndReturn200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);

        var transferenceDto = buildTransferenceDto(destinationAccount.getId());
        var valueAsString = objectMapper.writeValueAsString(transferenceDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(TRANSFERENCE_URL + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.sender").value(account.getOwnerName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.addressee").value(destinationAccount.getOwnerName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.value").value(TRANSFER_VALUE));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void transfer_NonExistingAccountId_ShouldNotTransferAndReturn404Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);

        var transferenceDto = buildTransferenceDto(destinationAccount.getId());
        var valueAsString = objectMapper.writeValueAsString(transferenceDto);

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(TRANSFERENCE_URL + NON_EXISTING_ACCOUNT_ID)
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
    void transfer_NonExistingDestinationAccountId_ShouldNotTransferAndReturn404Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        accountRepository.save(createdDestinationAccount);

        var transferenceDto = buildNonExistingDestinationAccountTransferenceDto();
        var valueAsString = objectMapper.writeValueAsString(transferenceDto);

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(TRANSFERENCE_URL + account.getId())
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
    void transfer_SameAccountId_ShouldNotTransferAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var account = accountRepository.save(createdAccount);

        var transferenceDto = buildTransferenceDto(account.getId());
        var valueAsString = objectMapper.writeValueAsString(transferenceDto);

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(TRANSFERENCE_URL + account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(SAME_ACCOUNT_ID_ERROR_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.BAD_REQUEST.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value(SAME_ACCOUNT_ID_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.IllegalArgumentException")
                .value(SAME_ID_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void transfer_InvalidTransferValue_ShouldNotTransferAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);

        var transferenceDto = buildInvalidTransferValueTransferenceDto(destinationAccount.getId());
        var valueAsString = objectMapper.writeValueAsString(transferenceDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(TRANSFERENCE_URL + account.getId())
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
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.value")
                .value(INVALID_TRANSFER_VALUE_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void transfer_InvalidDestinationAccountId_ShouldNotTransferAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        accountRepository.save(createdDestinationAccount);

        var transferenceDto = buildInvalidDestinationAccountIdTransferenceDto();
        var valueAsString = objectMapper.writeValueAsString(transferenceDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(TRANSFERENCE_URL + account.getId())
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
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.destinationAccountId")
                .value(INVALID_DESTINATION_ACCOUNT_ID_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void Withdraw_ValidTransference_ShouldWithdrawAndReturn200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);

        var withdrawDto = buildWithdrawDto();
        var valueAsString = objectMapper.writeValueAsString(withdrawDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(WITHDRAW_URL + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(account.getOwnerName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.withdrawalAmount").value(-WITHDRAW_VALUE));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void Withdraw_NonExistingAccountId_ShouldNotWithdrawAndReturn404Status() throws Exception {
        // given
        var withdrawDto = buildWithdrawDto();
        var valueAsString = objectMapper.writeValueAsString(withdrawDto);

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(WITHDRAW_URL + NON_EXISTING_ACCOUNT_ID)
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
    void Withdraw_NullValue_ShouldNotWithdrawAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);

        var withdrawDto = buildNullValueWithdrawDto();
        var valueAsString = objectMapper.writeValueAsString(withdrawDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(WITHDRAW_URL + account.getId())
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
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.value")
                .value(VALUE_MUST_NOT_BE_NULL_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void Withdraw_InvalidValue_ShouldNotWithdrawAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);

        var withdrawDto = buildInvalidValueWithdrawDto();
        var valueAsString = objectMapper.writeValueAsString(withdrawDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(WITHDRAW_URL + account.getId())
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
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.value")
                .value(INVALID_WITHDRAW_VALUE_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deposit_ValidTransference_ShouldDepositAndReturn200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);

        var depositDto = buildDepositDto();
        var valueAsString = objectMapper.writeValueAsString(depositDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(DEPOSIT_URL + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(valueAsString));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(account.getOwnerName()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.depositAmount").value(DEPOSIT_VALUE));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deposit_NonExistingAccountId_ShouldNotDepositAndReturn404Status() throws Exception {
        // given
        var depositDto = buildDepositDto();
        var valueAsString = objectMapper.writeValueAsString(depositDto);

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(DEPOSIT_URL + NON_EXISTING_ACCOUNT_ID)
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
    void deposit_NullValue_ShouldNotDepositAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);

        var depositDto = buildNullValueDepositDto();
        var valueAsString = objectMapper.writeValueAsString(depositDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(DEPOSIT_URL + account.getId())
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
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.value")
                .value(VALUE_MUST_NOT_BE_NULL_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deposit_InvalidValue_ShouldNotDepositAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);

        var depositDto = buildInvalidValueDepositDto();
        var valueAsString = objectMapper.writeValueAsString(depositDto);

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.post(DEPOSIT_URL + account.getId())
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
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.value")
                .value(INVALID_WITHDRAW_VALUE_MSG));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getBankStatement_AllFilters_ShouldReturnAllMatchingStatementsAnd200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);
        var transference2 = buildTransference(account, transactionOperator);
        var transference3 = buildWithdraw(account);
        var transference4 = buildDeposit(account);

        persistTransferences(transference, transference2, transference3, transference4);

        var initDate = transference.getTransferenceDate().minusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();
        var endDate = LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();
        var transferenceDate = transference.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference2Date = transference2.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(BANK_STATEMENT_URL + account.getId() +
                        "?initDate=" + initDate +
                        "&endDate=" + endDate +
                        "&transactionOperator=" + transactionOperator)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].operationDate").value(transferenceDate));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].operationDate")
                .value(transference2Date));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getBankStatement_TransactionOperatorFilter_ShouldReturnAllMatchingStatementsAnd200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);
        var transference2 = buildTransference(account, transactionOperator);
        var transference3 = buildWithdraw(account);
        var transference4 = buildDeposit(account);
        var transference5 = buildTransference(account, transactionOperator);

        persistTransferences(transference, transference2, transference3, transference4, transference5);

        var transferenceDate = transference.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference2Date = transference2.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference5Date = transference5.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(BANK_STATEMENT_URL + account.getId() +
                        "?transactionOperator=" + transactionOperator)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].operationDate").value(transferenceDate));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].operationDate")
                .value(transference2Date));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].operationDate")
                .value(transference5Date));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getBankStatement_DatesFilters_ShouldReturnAllMatchingStatementsAnd200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);
        var transference2 = buildTransference(account, transactionOperator);
        var transference3 = buildWithdraw(account);
        var transference4 = buildDeposit(account);

        persistTransferences(transference, transference2, transference3, transference4);

        var initDate = transference.getTransferenceDate().minusMinutes(1).truncatedTo(ChronoUnit.SECONDS).toString();
        var endDate = LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS).toString();
        var transferenceDate = transference.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference2Date = transference2.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference3Date = transference3.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference4Date = transference4.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(BANK_STATEMENT_URL + account.getId() +
                        "?initDate=" + initDate +
                        "&endDate=" + endDate)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].operationDate").value(transferenceDate));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].operationDate")
                .value(transference2Date));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].type")
                .value(Type.WITHDRAW.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].value").value(-WITHDRAW_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].operationDate")
                .value(transference3Date));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[3].type")
                .value(Type.DEPOSIT.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[3].value").value(DEPOSIT_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[3].operationDate")
                .value(transference4Date));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getBankStatement_NonFilters_ShouldReturnAllMatchingStatementsAnd200Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);
        var transference2 = buildTransference(account, transactionOperator);
        var transference3 = buildWithdraw(account);
        var transference4 = buildDeposit(account);

        persistTransferences(transference, transference2, transference3, transference4);

        var transferenceDate = transference.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference2Date = transference2.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference3Date = transference3.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();
        var transference4Date = transference4.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(MockMvcRequestBuilders.get(BANK_STATEMENT_URL + account.getId())
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[0].operationDate").value(transferenceDate));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].type")
                .value(Type.TRANSFERENCE.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TRANSFER_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[1].operationDate")
                .value(transference2Date));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].type")
                .value(Type.WITHDRAW.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].value").value(-WITHDRAW_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[2].operationDate")
                .value(transference3Date));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[3].type")
                .value(Type.DEPOSIT.toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[3].value").value(DEPOSIT_VALUE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$[3].operationDate")
                .value(transference4Date));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getBankStatement_NonExistingId_ShouldNotReturnStatementsAndReturn404Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);

        persistTransferences(transference);

        var initDate = transference.getTransferenceDate().minusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();
        var endDate = LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(BANK_STATEMENT_URL + NON_EXISTING_ACCOUNT_ID +
                        "?initDate=" + initDate +
                        "&endDate=" + endDate +
                        "&transactionOperator=" + transactionOperator)
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
    void getBankStatement_InvalidDate_ShouldNotReturnStatementsAndReturn400Status() throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);

        persistTransferences(transference);

        var endDate = LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(BANK_STATEMENT_URL + account.getId() +
                                "?initDate=" + INVALID_DATE +
                                "&endDate=" + endDate +
                                "&transactionOperator=" + transactionOperator)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value(ERROR_CONVERTING_VALUE_TITLE));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.timeStamp").isNotEmpty());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.status")
                .value(HttpStatus.BAD_REQUEST.value()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value(METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION_MSG));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.details.ConversionFailedException")
                .isNotEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getBankStatement_NonExistingTransactionOperator_ShouldReturnEmptyStatementsListAndReturn200Status()
            throws Exception {
        // given
        var createdAccount = AccountResourceTest.buildAccountDto().toEntity();
        var createdDestinationAccount = AccountResourceTest.buildSecondAccountDto().toEntity();

        var account = accountRepository.save(createdAccount);
        var destinationAccount = accountRepository.save(createdDestinationAccount);
        var transactionOperator = destinationAccount.getOwnerName();

        var transference = buildTransference(account, transactionOperator);

        persistTransferences(transference);

        var initDate = transference.getTransferenceDate().minusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();
        var endDate = LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS).toString();

        // when
        var resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(BANK_STATEMENT_URL + account.getId() +
                                "?initDate=" + initDate +
                                "&endDate=" + endDate +
                                "&transactionOperator=" + NON_EXISTING_TRANSACTION_OPERATOR_NAME)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
        resultActions.andDo(MockMvcResultHandlers.print());
    }

    private TransferenceDto buildTransferenceDto(Integer destinationAccountId) {
        return new TransferenceDto(TRANSFER_VALUE, destinationAccountId);
    }

    private TransferenceDto buildNonExistingDestinationAccountTransferenceDto() {
        return new TransferenceDto(TRANSFER_VALUE, NON_EXISTING_ACCOUNT_ID);
    }

    private TransferenceDto buildInvalidTransferValueTransferenceDto(int destinationAccountId) {
        return new TransferenceDto(INVALID_TRANSFER_VALUE, destinationAccountId);
    }

    private TransferenceDto buildInvalidDestinationAccountIdTransferenceDto() {
        return new TransferenceDto(TRANSFER_VALUE, INVALID_DESTINATION_ACCOUNT_ID);
    }

    private WithdrawDto buildWithdrawDto() {
        return new WithdrawDto(WITHDRAW_VALUE);
    }

    private WithdrawDto buildNullValueWithdrawDto() {
        return new WithdrawDto(null);
    }

    private WithdrawDto buildInvalidValueWithdrawDto() {
        return new WithdrawDto(INVALID_WITHDRAW_VALUE);
    }

    private DepositDto buildDepositDto() {
        return new DepositDto(DEPOSIT_VALUE);
    }

    private DepositDto buildNullValueDepositDto() {
        return new DepositDto(null);
    }

    private DepositDto buildInvalidValueDepositDto() {
        return new DepositDto(INVALID_DEPOSIT_VALUE);
    }

    private Transference buildTransference(Account account, String transactionOperatorName) {
        return new Transference(TRANSFER_VALUE, Type.TRANSFERENCE, account, transactionOperatorName);
    }

    private Transference buildWithdraw(Account account) {
        return new Transference(-WITHDRAW_VALUE, Type.WITHDRAW, account, null);
    }

    private Transference buildDeposit(Account account) {
        return new Transference(DEPOSIT_VALUE, Type.DEPOSIT, account, null);
    }

    private void persistTransferences(Transference transference, Transference... transferences) {
        transferenceRepository.save(transference);
        transferenceRepository.saveAll(Arrays.asList(transferences));
    }
}
