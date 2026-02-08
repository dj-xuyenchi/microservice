# Sử dụng base image OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy

# Tạo thư mục làm việc trong container
WORKDIR /eureka-server

# Copy file jar vào container
COPY eureka-server-0.0.1-SNAPSHOT.jar eureka-server-0.0.1-SNAPSHOT.jar

# Mở cổng Eureka
EXPOSE 8761

# Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "eureka-server-0.0.1-SNAPSHOT.jar"]
