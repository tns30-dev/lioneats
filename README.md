# Project Overview

Lioneats project is a comprehensive full-stack solution comprising a web application, a mobile application, and a backend service to deliver a seamless dining experience. The key components and functionalities include:

- **Web Application (React):** An interactive web interface that allows users to explore Singaporean dishes, find recommended restaurants, and perform searches based on location, dietary preferences (allergies), and budget constraints.

- **Mobile Application (Android):** A mobile-friendly platform offering similar features, optimized for on-the-go usage.

- **Image Recognition Model (Python, Keras):** A CNN-based model designed to identify and classify Singaporean dishes accurately. This model enhances the user experience by providing image-based dish recognition.

- **Backend Service (Spring Boot):** A robust and scalable backend that integrates with the web and mobile applications. It handles API requests, manages user data, processes search queries, and connects with external services like the Google Places API for restaurant recommendations.

### Core Features
1. **Image Classification:**
   - Recognize 10 popular Singaporean dishes using the Keras-powered CNN model.

2. **Restaurant Recommendations:**
   - Suggest restaurants based on dish preferences, location, and user constraints (allergies, budget).

3. **Search and Filter Options:**
   - Enable advanced search functionality with filters for location, price range, and dietary requirements.

4. **Full-Stack Integration:**
   - Seamlessly connect the frontend (React and Android) with the backend (Spring Boot).

5. **Third-Party API Integration:**
   - Utilize the Google Places API for nearby restaurant data and Azure Blob Storage for managing images.

# Demo video
https://youtu.be/EJD8-38Ycz8

# Files included:

1. FE Web (React) - web-frontend
2. FE Mobile (Android Java) - mobile-frontend
3. BE (Java Spring Boot) - backend
4. ML model (Python, Keras, Flask) - cnn-model

# Repository Setup

##To clone this repository, follow these steps:

1. **Open your terminal or command prompt.**
2. **Navigate to the directory where you want to save the project.**
      ```bash
      cd /path/to/your/directory
      ```
3. **Run the following command:**
      ```bash
      git clone git@github.com:tns30-dev/lioneats.git
      ```
4. **Navigate to the newly created project directory:**
      ```bash
      cd lioneats
      ```
	  
# Web Frontend - LionEats

## How to Install and Set Up the React App on Your Computer

1. **Navigate to the desired folder:**
   Open a terminal and navigate to the folder where you want the frontend app to reside.

2. **Ensure Vite is installed:**
   If Vite is not installed, you can install it globally using npm:
   ```bash
   npm install -g create-vite
   ```

3. **Create the React app:**
   Run the following command to set up the React app:
   ```bash
   npm create vite@latest lioneats-frontend
   ```
   Follow the on-screen instructions in the CLI to configure the project.

4. **Install additional dependencies:**
   Navigate into the app's directory and run the following commands to install required packages:
   ```bash
   npm install bootstrap --save
   npm install axios --save
   npm install react-router-dom --save
   ```

5. **Import Bootstrap CSS:**
   Add the following line to the `Main.jsx` file to include Bootstrap's CSS:
   ```javascript
   import 'bootstrap/dist/css/bootstrap.min.css';
   ```

6. **Run the development server:**
   Start the app locally by running:
   ```bash
   npm run dev
   ```
  
# Mobile Frontend - LionEats

### How to Set Up the Android App

1. Open the project in **Android Studio**.
2. Ensure you have **JDK 11+** and required SDKs installed via the **SDK Manager**.
3. Sync Gradle files by clicking **Sync Now**.
4. Set up an emulator (e.g., Pixel 4 API 29) or connect a physical device with **USB Debugging** enabled.
5. Run the app by clicking the **Run** button in Android Studio.

### Requirements

1. **Android Studio:** Ensure the latest version is installed.
2. **JDK 11 or higher:** Required for the project to build successfully.
3. **Gradle:** Sync all dependencies via Gradle.
4. **Device Setup:** Configure either an emulator (Pixel 4 API 29) or a physical Android device.


# CNN Model - LionEats

Our code is designed to recognize 10 Singaporean dishes using a CNN model.

## Steps to Set Up

1. **Python Version:**
   Ensure your Python version is 3.8 or higher.

2. **Install Required Libraries:**
   Install the following Python libraries:
   ```bash
   pip install sklearn tensorflow keras numpy pillow pandas
   ```

3. **Azure SDK:**
   Install the Azure Machine Learning SDK for Python to enable deployment on Azure:
   ```bash
   pip install azureml-ai
   ```

4. **Update File Paths:**
   Modify all file directory paths in `prediction.py` to match your local environment.

5. **Run the Script:**
   Execute `prediction.py` to start the model prediction process.

### Recognized Dishes
- Chicken Rice
- Laksa
- Hokkien Mee
- Char Kway Teow
- Roti Prata
- Satay
- Nasi Lemak
- Bak Kut Teh
- Kaya Toast
- Chilli Crab


# Java Spring Boot Backend - LionEats

## How to Set Up the Spring Boot Application on Your Computer

### Prerequisites
1. **Java Development Kit (JDK):** Ensure JDK 11 or higher is installed.
2. **Maven or Gradle:** Install the build tool used in the project.
3. **Database:** Install MySQL or another compatible database.
4. **IDE:** Use IntelliJ IDEA, Eclipse, or any preferred Java IDE.

### Steps to Set Up

1. **Open in IDE:**
   - Open the project in your preferred IDE (e.g., IntelliJ IDEA).

2. **Set Up the Database:**
   - Create a database named `lioneatsDatabase` (or as specified in `application.properties`).
   - Update the database credentials in `src/main/resources/application.properties`:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/lioneatsDatabase
     spring.datasource.username=(your_username)
     spring.datasource.password=(your_password)
     ```

3. **Add Google API Key:**
   - Open the `src/main/resources/application.properties` file.
   - Populate the `google.api.key` property with your Google API key:
     ```properties
     google.api.key=(insert your Google API key here)
     ```
   - Ensure the Google API key has permissions for services like Places API.

4. **Build the Project:**
     ```bash
     mvn clean install
     ```

5. **Run the Application:**
   - Start the Spring Boot application:
     ```bash
     mvn spring-boot:run
     ```

6. **Access the Application:**
   - The application runs on `http://localhost:8080` by default.
   
#Credits

1. Built by Soh Yong Sheng, Thet Naung Soe, Chen Yiqiu (Sophie), Zhao Ziyang, Sun Tianrui (Ray) and Lin Zeyu
2. Submitted as capstone project for NUS-ISS Graduate Diploma in Systems Analysis (SA58)
   

   
   
   
   
   
   
   
 



   

















