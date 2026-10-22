#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
source_file="kmp/local.properties"
url="$(sed -n 's/^supabase\.url=//p' "$source_file" | head -n1)"
if [ -z "$url" ]; then echo "missing supabase.url in kmp/local.properties" >&2; exit 1; fi
content_base_url="${url%/}/storage/v1/object/public/course/"
# Vite reads this for both `npm run dev` and production builds. It is public
# bucket configuration, derived locally rather than committed with a project URL.
printf 'VITE_CONTENT_BASE_URL=%s\n' "$content_base_url" > web/.env.local
./kmp/gradlew -q -p kmp :shared:jsBrowserProductionLibraryDistribution
rm -rf web/public/shared
mkdir -p web/public/shared
cp kmp/shared/build/dist/js/productionLibrary/*.{js,d.ts,map} web/public/shared/ 2>/dev/null || true
cp kmp/shared/build/dist/js/productionLibrary/package.json web/public/shared/
export VITE_CONTENT_BASE_URL="$content_base_url"
