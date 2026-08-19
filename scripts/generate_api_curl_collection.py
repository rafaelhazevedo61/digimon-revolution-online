from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
CONTROLLERS = ROOT / "backend" / "src" / "main" / "java"
CURL_OUTPUT = ROOT / "backend" / "src" / "main" / "resources" / "api-curl-collection.sh"
POSTMAN_OUTPUT = ROOT / "backend" / "src" / "main" / "resources" / "collection" / "DRO - MODULES.postman_collection.json"

MAPPING_RE = re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping(?:\("([^\"]*)"\))?')
REQUEST_RE = re.compile(r'@RequestMapping(?:\("([^\"]*)"\))?')
CLASS_RE = re.compile(r'class\s+(\w+)')
METHOD_RE = re.compile(r'public\s+.*?\s+(\w+)\s*\(')
PARAM_RE = re.compile(r'@RequestParam(?:\([^)]*\))?\s+(?:final\s+)?[\w<>, ?\[\]]+\s+(\w+)')
PATH_PARAM_RE = re.compile(r'\{([^}]+)\}')

BODY_EXAMPLES: dict[tuple[str, str], dict[str, Any]] = {
    ("/auth/register", "register"): {
        "username": "jogador.teste",
        "email": "jogador@example.com",
        "password": "troque-esta-senha",
    },
    ("/auth/login", "login"): {
        "email": "jogador@example.com",
        "password": "troque-esta-senha",
    },
    ("/mail", "send"): {
        "recipientUsername": "jogador-alvo",
        "subject": "Mensagem de teste",
        "body": "Conteúdo da mensagem de teste.",
    },
    ("/mail/{messageId}/action", "action"): {"action": "ACCEPT"},
    ("/clans/{id}/invite", "invite"): {"username": "jogador-alvo"},
    ("/clans", "create"): {
        "name": "Clã de Teste",
        "tag": "TEST",
        "description": "Clã criado para testes.",
    },
    ("/admin/mail/announcements", "createAnnouncement"): {
        "subject": "Manutenção programada",
        "body": "Comunicado de teste. Altere este texto antes de enviar.",
    },
    ("/admin/mail/event-rewards", "endpoint"): {
        "playerUsername": "jogador-alvo",
        "sourceType": "EVENT",
        "sourceId": "evento-teste-001",
        "subject": "Premiação de teste",
        "body": "Você recebeu esta recompensa pelo evento.",
        "bitsAmount": 5000,
        "itemType": "TRAINING_STONE",
        "itemQuantity": 2,
        "validityDays": 7,
    },
}


def normalize(*parts: str | None) -> str:
    value = "/".join(part.strip("/") for part in parts if part)
    return "/" + value if value else "/"


def to_variable(name: str) -> str:
    return re.sub(r'(?<!^)([A-Z])', r'_\1', name).upper()


def shell_path(path: str) -> str:
    def replace(match: re.Match[str]) -> str:
        return "${" + to_variable(match.group(1)) + "}"

    return re.sub(r'\{([^}]+)\}', replace, path)


def postman_path(path: str) -> str:
    return re.sub(r'\{([^}]+)\}', lambda match: "{{" + match.group(1) + "}}", path)


def parse_controller(path: Path) -> list[dict[str, Any]]:
    lines = path.read_text(encoding="utf-8").splitlines()
    class_name = path.stem
    class_prefix = ""
    endpoints: list[dict[str, Any]] = []

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
        method_found = False
        for following in lines[index + 1 : index + 16]:
            signature += " " + following.strip()
            method_match = METHOD_RE.search(signature)
            if method_match and not method_found:
                method_name = method_match.group(1)
                method_found = True

        query_params = PARAM_RE.findall(signature)
        endpoints.append({
            "controller": class_name,
            "method": method,
            "path": endpoint_path,
            "name": method_name,
            "query_params": query_params,
            "auth_required": "@RequestHeader" in signature,
            "has_body": "@RequestBody" in signature,
        })

    return endpoints


def collect_endpoints() -> list[dict[str, Any]]:
    endpoints: list[dict[str, Any]] = []
    for controller in sorted(CONTROLLERS.rglob("*Controller.java")):
        endpoints.extend(parse_controller(controller))
    return sorted(endpoints, key=lambda item: (item["path"], item["method"], item["controller"], item["name"]))


def all_parameters(endpoints: list[dict[str, Any]]) -> list[str]:
    names: set[str] = set()
    for endpoint in endpoints:
        names.update(PATH_PARAM_RE.findall(endpoint["path"]))
        names.update(endpoint["query_params"])
    return sorted(names)


def curl_auth(endpoint: dict[str, Any], path: str) -> str:
    if path.startswith("/auth"):
        return ""
    if path.startswith("/admin"):
        return ' -H "Authorization: Bearer ${ADMIN_TOKEN}"'
    if endpoint["auth_required"]:
        return ' -H "Authorization: Bearer ${TOKEN}"'
    return ""


def body_for(endpoint: dict[str, Any]) -> dict[str, Any] | None:
    if not endpoint["has_body"]:
        return None
    return BODY_EXAMPLES.get((endpoint["path"], endpoint["name"])) \
        or BODY_EXAMPLES.get((endpoint["path"], "endpoint"), {})


def query_suffix(endpoint: dict[str, Any], variable_style: str) -> str:
    if not endpoint["query_params"]:
        return ""
    if variable_style == "shell":
        return "?" + "&".join(
            f"{parameter}=${{{to_variable(parameter)}}}" for parameter in endpoint["query_params"]
        )
    return "?" + "&".join(
        f"{parameter}={{{{{parameter}}}}}" for parameter in endpoint["query_params"]
    )


def generate_curl_collection(endpoints: list[dict[str, Any]]) -> None:
    parameters = all_parameters(endpoints)
    lines = [
        "#!/usr/bin/env bash",
        "# Collection oficial de exemplos curl do Digimon Revolution Online.",
        "# Gerada a partir dos controllers Java; execute scripts/generate_api_curl_collection.py após alterar endpoints.",
        "# Os payloads '{}' são exemplos: revise-os antes de executar.",
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
        "EQUIPMENT_ID", "PLAYER_ID",
    }
    for parameter in parameters:
        variable = to_variable(parameter)
        if variable in known_variables:
            continue
        default = "00000000-0000-0000-0000-000000000000" if variable.endswith("ID") else "VALOR"
        lines.append(f'{variable}="${{{variable}:-{default}}}"')
    lines.extend([
        "",
        "# Autenticação: substitua TOKEN/ADMIN_TOKEN antes de executar comandos protegidos.",
        "# Endpoints públicos não precisam do header Authorization.",
        "",
    ])

    current_group = ""
    for endpoint in endpoints:
        group = endpoint["path"].split("/", 2)[1] if endpoint["path"].count("/") >= 2 else "root"
        if group != current_group:
            current_group = group
            lines.extend([f"# ===== {group.upper()} =====", ""])

        path = shell_path(endpoint["path"])
        auth = curl_auth(endpoint, path)
        body = body_for(endpoint)
        content = ' -H "Content-Type: application/json"' if body is not None else ""
        body_arg = f" -d '{json.dumps(body, ensure_ascii=False)}'" if body is not None else ""
        query = query_suffix(endpoint, "shell")
        lines.append(f"# {endpoint['controller']}.{endpoint['name']} ({endpoint['method']} {endpoint['path']})")
        lines.append(f'# curl --fail-with-body -i -X {endpoint["method"]} "${{BASE_URL}}{path}{query}"{auth}{content}{body_arg}')
        lines.append("")

    CURL_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    CURL_OUTPUT.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")
    CURL_OUTPUT.chmod(0o755)


def generate_postman_collection(endpoints: list[dict[str, Any]]) -> None:
    parameters = all_parameters(endpoints)
    variables = [
        {"key": "baseUrl", "value": "http://localhost:8080"},
        {"key": "playerToken", "value": ""},
        {"key": "adminToken", "value": ""},
    ]
    for parameter in parameters:
        variables.append({"key": parameter, "value": ""})

    folders: dict[str, list[dict[str, Any]]] = {}
    for endpoint in endpoints:
        group = endpoint["path"].split("/", 2)[1] if endpoint["path"].count("/") >= 2 else "root"
        path = postman_path(endpoint["path"])
        raw_url = "{{baseUrl}}" + path + query_suffix(endpoint, "postman")
        headers: list[dict[str, str]] = []
        if endpoint["path"].startswith("/admin"):
            headers.append({"key": "Authorization", "value": "Bearer {{adminToken}}", "type": "text"})
        elif endpoint["auth_required"]:
            headers.append({"key": "Authorization", "value": "Bearer {{playerToken}}", "type": "text"})

        request: dict[str, Any] = {
            "method": endpoint["method"],
            "header": headers,
            "url": raw_url,
            "description": f"Gerado de {endpoint['controller']}.{endpoint['name']}. Revise IDs e valores antes de enviar.",
        }
        body = body_for(endpoint)
        if body is not None:
            request["header"].append({"key": "Content-Type", "value": "application/json", "type": "text"})
            request["body"] = {
                "mode": "raw",
                "raw": json.dumps(body, ensure_ascii=False, indent=2),
                "options": {"raw": {"language": "json"}},
            }

        folders.setdefault(group, []).append({
            "name": endpoint["name"],
            "request": request,
            "response": [],
        })

    collection = {
        "info": {
            "_postman_id": "5e8e17b8-fee5-49bb-8f44-e7634ef6fe39",
            "name": "DRO - MODULES",
            "description": "Collection gerada automaticamente a partir dos controllers do Digimon Revolution Online. Tokens ficam como variáveis locais do Postman.",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        },
        "variable": variables,
        "item": [
            {"name": group, "item": items}
            for group, items in sorted(folders.items())
        ],
    }
    POSTMAN_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    POSTMAN_OUTPUT.write_text(json.dumps(collection, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    endpoints = collect_endpoints()
    generate_curl_collection(endpoints)
    generate_postman_collection(endpoints)
    print(f"Generated {len(endpoints)} endpoints in {CURL_OUTPUT} and {POSTMAN_OUTPUT}")


if __name__ == "__main__":
    main()
