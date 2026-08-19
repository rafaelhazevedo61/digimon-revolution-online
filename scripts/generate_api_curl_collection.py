from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONTROLLERS = ROOT / "backend" / "src" / "main" / "java"
OUTPUT = ROOT / "backend" / "src" / "main" / "resources" / "api-curl-collection.sh"

MAPPING_RE = re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping(?:\("([^\"]*)"\))?')
REQUEST_RE = re.compile(r'@RequestMapping(?:\("([^\"]*)"\))?')
CLASS_RE = re.compile(r'class\s+(\w+)')
METHOD_RE = re.compile(r'public\s+[^ (]+(?:<[^>]+>)?\s+(\w+)\s*\(')
PARAM_RE = re.compile(r'@RequestParam(?:\([^)]*\))?\s+(?:final\s+)?[\w<>, ?\[\]]+\s+(\w+)')
PATH_PARAM_RE = re.compile(r'\{([^}]+)\}')


def normalize(*parts: str | None) -> str:
    value = "/".join(part.strip("/") for part in parts if part)
    return "/" + value if value else "/"


def to_variable(name: str) -> str:
    return re.sub(r'(?<!^)([A-Z])', r'_\1', name).upper()


def shell_path(path: str) -> str:
    def replace(match: re.Match[str]) -> str:
        return "${" + to_variable(match.group(1)) + "}"

    return re.sub(r'\{([^}]+)\}', replace, path)


def parse_controller(path: Path) -> list[dict[str, str]]:
    lines = path.read_text(encoding="utf-8").splitlines()
    class_name = path.stem
    class_prefix = ""
    endpoints: list[dict[str, str]] = []

    for index, line in enumerate(lines):
        class_match = CLASS_RE.search(line)
        if class_match:
            class_name = class_match.group(1)

        request_match = REQUEST_RE.search(line)
        if request_match and "class" not in line:
            class_prefix = request_match.group(1) or ""

        mapping_match = MAPPING_RE.search(line)
        if not mapping_match:
            continue

        method = mapping_match.group(1).upper()
        method_path = mapping_match.group(2) or ""
        endpoint_path = normalize(class_prefix, method_path)
        method_name = "endpoint"
        signature = ""
        for following in lines[index + 1 : index + 12]:
            signature += " " + following.strip()
            method_match = METHOD_RE.search(signature)
            if method_match:
                method_name = method_match.group(1)
                break

        query_params = PARAM_RE.findall(signature)
        endpoints.append({
            "controller": class_name,
            "method": method,
            "path": endpoint_path,
            "name": method_name,
            "query_params": query_params,
        })

    return endpoints


def main() -> None:
    endpoints: list[dict[str, str]] = []
    for controller in sorted(CONTROLLERS.rglob("*Controller.java")):
        endpoints.extend(parse_controller(controller))

    endpoints.sort(key=lambda item: (item["path"], item["method"], item["controller"], item["name"]))
    parameter_names = set()
    for endpoint in endpoints:
        parameter_names.update(PATH_PARAM_RE.findall(endpoint["path"]))
        parameter_names.update(endpoint["query_params"])

    lines = [
        "#!/usr/bin/env bash",
        "# Collection oficial de exemplos curl do Digimon Revolution Online.",
        "# Gerada a partir dos controllers Java; execute scripts/generate_api_curl_collection.py após alterar endpoints.",
        "# Os payloads '{}' são placeholders nos endpoints que exigem JSON.",
        "# Por segurança, as chamadas estão comentadas: descomente apenas o curl que deseja testar.",
        "# Não execute este arquivo inteiro; ele contém operações de criação, compra, exclusão e administração.",
        "",
        'BASE_URL="${BASE_URL:-http://localhost:8080}"',
        'TOKEN="${TOKEN:-COLE_SEU_TOKEN_DE_JOGADOR_AQUI}"',
        'ADMIN_TOKEN="${ADMIN_TOKEN:-COLE_SEU_TOKEN_DE_ADMIN_AQUI}"',
        'ID="${ID:-00000000-0000-0000-0000-000000000000}"',
        'USERNAME="${USERNAME:-nome-do-jogador}"',
        'CODE="${CODE:-CODIGO}"',
        'CLAN_ID="${CLAN_ID:-00000000-0000-0000-0000-000000000000}"',
        'MESSAGE_ID="${MESSAGE_ID:-00000000-0000-0000-0000-000000000000}"',
        'MISSION_ID="${MISSION_ID:-00000000-0000-0000-0000-000000000000}"',
        'INSTANCE_ID="${INSTANCE_ID:-00000000-0000-0000-0000-000000000000}"',
        'LISTING_ID="${LISTING_ID:-00000000-0000-0000-0000-000000000000}"',
        'DIGIMON_ID="${DIGIMON_ID:-00000000-0000-0000-0000-000000000000}"',
        'EQUIPMENT_ID="${EQUIPMENT_ID:-00000000-0000-0000-0000-000000000000}"',
        'PLAYER_ID="${PLAYER_ID:-00000000-0000-0000-0000-000000000000}"',
    ]
    known_variables = {
        "BASE_URL", "TOKEN", "ADMIN_TOKEN", "ID", "USERNAME", "CODE", "CLAN_ID",
        "MESSAGE_ID", "MISSION_ID", "INSTANCE_ID", "LISTING_ID", "DIGIMON_ID",
        "EQUIPMENT_ID", "PLAYER_ID"
    }
    for parameter in sorted(parameter_names):
        variable = to_variable(parameter)
        if variable in known_variables:
            continue
        default = "00000000-0000-0000-0000-000000000000" if variable.endswith("ID") else "VALOR"
        lines.append(f'{variable}="${{{variable}:-{default}}}"')
    lines.extend([
        "",
        "# Autenticação: substitua TOKEN/ADMIN_TOKEN antes de executar comandos protegidos.",
        "# Endpoints públicos podem ser executados sem o header Authorization.",
        "",
    ])

    current_group = ""
    for endpoint in endpoints:
        group = endpoint["path"].split("/", 2)[1] if endpoint["path"].count("/") >= 2 else "root"
        if group != current_group:
            current_group = group
            lines.extend([f"# ===== {group.upper()} =====", ""])

        path = shell_path(endpoint["path"])
        auth_var = "ADMIN_TOKEN" if path.startswith("/admin") else "TOKEN"
        auth = "" if path.startswith("/auth") else f' -H "Authorization: Bearer ${{{auth_var}}}"'
        content = ""
        body = ""
        if endpoint["method"] in {"POST", "PUT", "PATCH"}:
            content = ' -H "Content-Type: application/json"'
            body = " -d '{}'"
        query = ""
        if endpoint["query_params"]:
            query = "?" + "&".join(
                f"{parameter}=${{{to_variable(parameter)}}}" for parameter in endpoint["query_params"]
            )

        lines.append(f"# {endpoint['controller']}.{endpoint['name']} ({endpoint['method']} {endpoint['path']})")
        lines.append(f'# curl --fail-with-body -i -X {endpoint["method"]} "${{BASE_URL}}{path}{query}"{auth}{content}{body}')
        lines.append("")

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")
    OUTPUT.chmod(0o755)
    print(f"Generated {len(endpoints)} curl commands at {OUTPUT}")


if __name__ == "__main__":
    main()
