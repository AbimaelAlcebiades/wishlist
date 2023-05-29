# Imagem base com Java 17
FROM openjdk:17
# Diretório de trabalho dentro do contêiner
WORKDIR /app

# Copiar o arquivo pom.xml para o diretório de trabalho
COPY pom.xml .

# Baixar as dependências do Maven
RUN mvn dependency:go-offline

# Copiar todo o código-fonte para o diretório de trabalho
COPY . .

# Compilar o projeto
RUN mvn package -DskipTests

# Expor a porta da aplicação
EXPOSE 8080

# Comando para iniciar a aplicação
CMD ["java", "-jar", "target/nome-do-seu-arquivo-jar.jar"]
