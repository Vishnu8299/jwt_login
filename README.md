# 🏢 MSME Web Application

A **full-stack web application** designed for **Micro, Small & Medium Enterprises (MSMEs)**, featuring **secure JWT authentication**, a modern UI, and robust backend services.

---

## 📁 Project Structure

```
.
├── backend/          # Spring Boot Backend with JWT Authentication
└── frontend/         # Responsive MSME Webpage UI
```

---

## 🌐 Frontend (MSME Webpage)

### 🚀 Features
✅ Modern, **responsive design**
✅ Interactive UI components
✅ MSME-focused **content sections**
✅ **Contact forms** for user inquiries
✅ Services **showcase & business insights**

### 🛠 Tech Stack
- **HTML5**, **CSS3**, **JavaScript**
- **Bootstrap** for responsiveness
- **jQuery** for interactive elements

### 📌 Setup Instructions
1. **Navigate to the frontend directory:**
   ```bash
   cd frontend
   ```
2. **Open `index.html`** in your web browser.

---

## 🔐 Backend (JWT Authentication API)

### 📌 Prerequisites
- **MongoDB** running on `localhost:27017`
- **Java 11** or higher
- **Maven** for dependency management
- **Postman** for API testing

### ⚙️ Setup Instructions

1. **Start MongoDB:**
   ```bash
   mongod
   ```
2. **Start the Spring Boot application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. **Import Postman collection:**
   - Open **Postman**
   - Click on **Import**
   - Select the `postman_collection.json` file

### 📡 API Endpoints

#### 🔑 Authentication
- `POST /api/auth/register` → **Register** a new user
- `POST /api/auth/login` → **User login**
- `POST /api/auth/refresh` → **Refresh access token**

#### 👤 Users
- `GET /api/users/profile` → **Retrieve user profile**
- `PUT /api/users/profile` → **Update user profile**

#### 📂 Projects
- `GET /api/projects` → **Fetch all projects**
- `POST /api/projects` → **Create a new project**
- `GET /api/projects/{id}` → **Fetch project by ID**
- `PUT /api/projects/{id}` → **Update project details**
- `DELETE /api/projects/{id}` → **Delete project**

### 🧪 Testing Guidelines
✅ Check **HTTP response codes**
✅ Verify **response data format**
✅ Test **both valid & invalid inputs**
✅ Try accessing **protected endpoints without a token**
✅ Validate **token expiration & refresh flow**

### 🔍 Common Issues & Fixes
❌ **401 Unauthorized?** → Token expired, re-login required.
❌ **MongoDB connection error?** → Ensure MongoDB is running on **port 27017**.
❌ **Email configuration issue?** → Verify email settings in `application.properties`.

---

## 🔗 Frontend & Backend Integration

1. **Update frontend API calls** to point to backend endpoints.
2. **Configure CORS settings** in backend to allow frontend access.
3. **Set up environment variables** for API URLs.

---

## 🚀 Deployment

### 🌐 Frontend Deployment
- Deploy static files to **GitHub Pages, Netlify, or Vercel**.
- Update API URLs in frontend JavaScript to match **backend deployment URL**.

### 🔐 Backend Deployment
- Deploy on **AWS, Heroku, or any cloud platform**.
- Configure **MongoDB in the cloud**.
- Use **environment variables** for production settings.

---

## 🤝 Contributing

1. **Fork** the repository.
2. **Create** a feature branch.
3. **Commit** your changes.
4. **Push** to your branch.
5. **Create** a **Pull Request**.

---

## 📜 License

This project is **licensed under the MIT License** – see the [LICENSE](LICENSE) file for details.

---

### 🌟 Stay Updated
📌 **Follow & Star ⭐ this repository** for the latest updates!

💡 **Happy Coding! 🚀**
