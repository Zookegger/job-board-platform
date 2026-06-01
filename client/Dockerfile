FROM node:24-alpine AS build
ENV CI=true

WORKDIR /app

RUN corepack enable && echo "PNPM VERSION:" && pnpm --version

RUN pnpm config set minimumReleaseAge 0

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .

RUN pnpm build

FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
