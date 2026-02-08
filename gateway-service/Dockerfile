# Sử dụng base image OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy

# Tạo thư mục làm việc trong container
WORKDIR /gateway

COPY gateway-0.0.1-SNAPSHOT.jar gateway-0.0.1-SNAPSHOT.jar

# Mở cổng 8762 (hoặc cổng mà ứng dụng sử dụng)
EXPOSE 8762

# Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "gateway-0.0.1-SNAPSHOT.jar"]
