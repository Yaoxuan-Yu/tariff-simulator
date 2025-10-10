package com.example.tariff.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.entity.Product;
import com.example.tariff.entity.Tariff;
import com.example.tariff.repository.ProductRepository;
import com.example.tariff.repository.TariffRepository;

@ExtendWith(MockitoExtension.class)
public class TariffServiceTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private TariffRepository tariffRepository;

        @InjectMocks
        private TariffService tariffService;

        private Product testProduct;
        private Tariff testTariffWithFTA;
        private Tariff testTariffWithoutFTA;

        @BeforeEach
        public void setUp() {
                // 初始化测试产品
                testProduct = new Product();
                testProduct.setName("Test Product");
                testProduct.setBrand("Test Brand");
                testProduct.setCost(10.0); // 单价10元
                testProduct.setUnit("piece");

                // 初始化有FTA协议的关税规则 (AHS税率)
                testTariffWithFTA = new Tariff();
                testTariffWithFTA.setCountry("China"); // 进口国
                testTariffWithFTA.setPartner("Singapore"); // 出口国
                testTariffWithFTA.setAhsWeighted(2.0); // FTA优惠税率 2%
                testTariffWithFTA.setMfnWeighted(10.0); // 最惠国税率 10%

                // 初始化无FTA协议的关税规则 (MFN税率)
                testTariffWithoutFTA = new Tariff();
                testTariffWithoutFTA.setCountry("China"); // 进口国
                testTariffWithoutFTA.setPartner("USA"); // 出口国
                testTariffWithoutFTA.setAhsWeighted(5.0);
                testTariffWithoutFTA.setMfnWeighted(15.0); // 最惠国税率 15%
        }

        @Test
        void calculate_Success_WithFTA() {
                // 模拟Repository行为
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                        .thenReturn(Optional.of(testTariffWithFTA));

                // 执行测试
                TariffResponse response = tariffService.calculate(
                        "Test Product", "Test Brand", "Singapore", "China", 3, null);

                // 验证结果
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
                assertEquals("Test Product", response.getData().getProduct());
                assertEquals(30.0, response.getData().getProductCost()); // 3件 * 10元 = 30元
                assertEquals(2.0, response.getData().getTariffRate()); // FTA税率 2%
                assertEquals(30.6, response.getData().getTotalCost()); // 30 + (30 * 2%) = 30.6元
        }

        @Test
        void calculate_Success_WithoutFTA() {
                // 模拟Repository行为
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("China", "USA"))
                        .thenReturn(Optional.of(testTariffWithoutFTA));

                // 执行测试
                TariffResponse response = tariffService.calculate(
                        "Test Product", "Test Brand", "USA", "China", 2, null);

                // 验证结果
                assertTrue(response.isSuccess());
                assertEquals(20.0, response.getData().getProductCost()); // 2件 * 10元 = 20元
                assertEquals(15.0, response.getData().getTariffRate()); // MFN税率 15%
                assertEquals(23.0, response.getData().getTotalCost()); // 20 + (20 * 15%) = 23元
        }

        @Test
        void calculate_ProductNotFound() {
                // 模拟产品不存在
                when(productRepository.findByNameAndBrand("Non-existent Product", "Test Brand"))
                        .thenReturn(List.of());

                // 执行测试 - 现在应该抛出异常
                assertThrows(com.example.tariff.exception.NotFoundException.class, () -> {
                        tariffService.calculate(
                                "Non-existent Product", "Test Brand", "Singapore", "China", 3, null);
                });
        }

        @Test
        void calculate_TariffNotFound() {
                // 模拟产品存在，但关税规则不存在
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("Mars", "Earth"))
                        .thenReturn(Optional.empty());

                // 执行测试 - 现在应该抛出异常
                assertThrows(com.example.tariff.exception.NotFoundException.class, () -> {
                        tariffService.calculate(
                                "Test Product", "Test Brand", "Earth", "Mars", 3, null);
                });
        }

        @Test
        void calculate_WithCustomCost() {
                // 模拟Repository行为
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                        .thenReturn(Optional.of(testTariffWithFTA));

                // 执行测试，使用自定义成本 15元
                TariffResponse response = tariffService.calculate(
                        "Test Product", "Test Brand", "Singapore", "China", 2, "15");

                // 验证结果
                assertTrue(response.isSuccess());
                assertEquals(30.0, response.getData().getProductCost()); // 2件 * 15元 = 30元
                assertEquals(30.6, response.getData().getTotalCost()); // 30 + (30 * 2%) = 30.6元
        }

        @Test
        void calculateWithMode_UserDefinedTariff_Success() {
                // 1. 准备测试数据
                String userTariffId = "user-12345";
                String productName = "Test Product";
                String brand = "Test Brand";
                String exportingFrom = "Singapore";
                String importingTo = "China";
                double quantity = 2;
                String customCost = "15"; // 自定义单价15元
                String mode = "user"; // 使用用户自定义模式

                // 2. 模拟Repository行为 - 确保产品能被找到
                when(productRepository.findByNameAndBrand(productName, brand))
                        .thenReturn(List.of(testProduct));

                // 3. 添加一个用户自定义的关税规则到service中
                TariffDefinitionsResponse.TariffDefinitionDto userTariff = new TariffDefinitionsResponse.TariffDefinitionDto();
                userTariff.setId(userTariffId);
                userTariff.setProduct(productName);
                userTariff.setExportingFrom(exportingFrom);
                userTariff.setImportingTo(importingTo);
                userTariff.setType("Custom Duty");
                userTariff.setRate(5.0); // 用户自定义的关税税率是5%

                // 调用service的方法添加用户自定义关税
                tariffService.addUserTariffDefinition(userTariff);

                // 4. 执行测试 - 调用带mode参数的calculate方法
                TariffResponse response = tariffService.calculateWithMode(
                        productName, brand, exportingFrom, importingTo, quantity, customCost, mode, userTariffId);

                // 5. 验证结果
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
                
                // 验证产品成本：2件 * 15元 = 30元
                assertEquals(30.0, response.getData().getProductCost());
                
                // 验证关税税率：使用的是用户自定义的5%
                assertEquals(5.0, response.getData().getTariffRate());
                
                // 验证总成本：30元 + (30元 * 5%) = 31.5元
                assertEquals(31.5, response.getData().getTotalCost());
                
                // 验证关税类型描述中包含"user-defined"
                assertTrue(response.getData().getTariffType().contains("user-defined"));
        }

        @Test
        void calculateWithMode_UserDefinedTariff_NotFound() {
                // 测试当用户自定义关税不存在时的情况
                String productName = "Test Product";
                String brand = "Test Brand";
                String exportingFrom = "Singapore";
                String importingTo = "China";
                double quantity = 2;
                String mode = "user";
                String nonExistentTariffId = "user-99999"; // 一个不存在的ID

                // 执行测试 - 使用一个不存在的用户关税ID
                TariffResponse response = tariffService.calculateWithMode(
                        productName, brand, exportingFrom, importingTo, quantity, null, mode, nonExistentTariffId);

                // 验证结果 - 应该返回失败
                assertFalse(response.isSuccess());
                assertEquals("Selected user-defined tariff not found or not applicable", response.getError());
        }

        @Test
        void calculateWithMode_InvalidMode() {
                when(productRepository.findByNameAndBrand("Test product", "Test brand"))
                .thenReturn(List.of(testProduct));
                
                assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
                tariffService.calculateWithMode("Test Product", "Test Brand", "Singapore", "China", 2, null, "invalid_mode", null);
        });
        }

        @Test
        void calculate_NullInputs_ShouldThrowValidationException() {
                assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
                tariffService.calculate(
                null, "Test Brand", "Singapore", "China", 1, null
                );
        });

        assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
        tariffService.calculate(
                "Test Product", null, "Singapore", "China", 1, null
                );
        });

        assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
        tariffService.calculate(
                "Test Product", "Test Brand", null, "China", 1, null
                );
        });
        }

        @Test
        void calculate_QuantityZero() {
                // 执行测试 - 现在应该抛出异常（不需要模拟Repository，因为验证在调用Repository之前）
                assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
                        tariffService.calculate(
                                "Test Product", "Test Brand", "Singapore", "China", 0, null);
                });
        }

        @Test
        void calculate_QuantityNegative() {
                // 执行测试 - 现在应该抛出异常（不需要模拟Repository，因为验证在调用Repository之前）
                assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
                        tariffService.calculate(
                                "Test Product", "Test Brand", "Singapore", "China", -5, null);
                });
        }

        @Test
        void calculate_CustomCostNegative() {
                // 模拟Repository行为
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                        .thenReturn(Optional.of(testTariffWithFTA));

                // 执行测试，使用自定义负成本
                TariffResponse response = tariffService.calculate(
                        "Test Product", "Test Brand", "Singapore", "China", 2, "-10");

                // 同样，根据业务逻辑判断结果
                assertTrue(response.isSuccess());
                assertEquals(-20.0, response.getData().getProductCost()); // 2件 * (-10元) = -20元
                assertEquals(-20.4, response.getData().getTotalCost()); // -20 + (-20 * 2%) = -20.4元
        }

        @Test
        void calculate_CustomCostNonNumeric() {
                // 模拟Repository行为
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                        .thenReturn(Optional.of(testTariffWithFTA));

                // 执行测试 - 现在应该抛出异常
                assertThrows(com.example.tariff.exception.ValidationException.class, () -> {
                        tariffService.calculate(
                                "Test Product", "Test Brand", "Singapore", "China", 2, "abc");
                });
        }

        @Test
        void calculate_VeryLargeQuantity() {
                // 模拟Repository行为
                when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
                        .thenReturn(List.of(testProduct));
                when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                        .thenReturn(Optional.of(testTariffWithFTA));

                // 执行测试，使用一个很大的数量
                double largeQuantity = 1_000_000;
                TariffResponse response = tariffService.calculate(
                        "Test Product", "Test Brand", "Singapore", "China", largeQuantity, null);

                // 验证结果
                assertTrue(response.isSuccess());
                assertEquals(10_000_000.0, response.getData().getProductCost()); // 1e6件 * 10元 = 1e7元
                assertEquals(10_200_000.0, response.getData().getTotalCost()); // 1e7 + (1e7 * 2%) = 10,200,000元
        }

}