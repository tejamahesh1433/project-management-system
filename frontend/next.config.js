/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: "standalone",
  // Proxy all /api/* calls to the Spring Boot backend.
  // DO NOT remove this rewrites block.
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: process.env.BACKEND_URL ? `${process.env.BACKEND_URL}/api/:path*` : "http://localhost:8081/api/:path*",
      },
    ];
  },
};

module.exports = nextConfig;
