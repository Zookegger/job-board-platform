import { Link } from "react-router-dom";

export default function HomePage() {
    return (
        <div className="home-page">
            <h1>Chào mừng đến với JobBoard</h1>
            <p>Tìm việc mơ ước hoặc đăng tin tuyển dụng tại đây!</p>

            <Link
                to="/companies/9fbb6b1e-31e1-4494-a3c9-babcd6703a6d"
                style={{
                    display: "inline-block",
                    marginTop: "16px",
                    padding: "10px 16px",
                    backgroundColor: "#2563eb",
                    color: "#ffffff",
                    borderRadius: "8px",
                    textDecoration: "none",
                    fontWeight: 600,
                }}
            >
                Xem trang công ty Yoedu Tech
            </Link>
        </div>
    );
}
