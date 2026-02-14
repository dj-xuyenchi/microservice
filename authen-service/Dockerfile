# Sử dụng base image OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy

# Tạo thư mục làm việc trong container
WORKDIR /authen-service

COPY authen-service-0.0.1-SNAPSHOT.jar authen-service-0.0.1-SNAPSHOT.jar

# Mở cổng 9004 (hoặc cổng mà ứng dụng sử dụng)
EXPOSE 9004

# Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "authen-service-0.0.1-SNAPSHOT.jar"]
