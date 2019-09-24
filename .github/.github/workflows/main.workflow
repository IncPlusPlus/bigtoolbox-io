workflow "Update submodule on parent repo" {
  on = "push"
  resolves = ["HTTP client"]
}

action "Filters for GitHub Actions" {
  uses = "actions/bin/filter@25b7b846d5027eac3315b50a8055ea675e2abd89"
  args = "branch master"
}

action "HTTP client" {
  uses = "swinton/httpie.action@69125d73caa2c6821f6a41a86112777a37adc171"
  needs = ["Filters for GitHub Actions"]
  secrets = ["GITHUB_TOKEN"]
  args = ["--auth-type=jwt", "--auth=${{ secrets.GITHUB_TOKEN }}", "POST", "api.github.com/repos/IncPlusPlus/bigtoolbox/dispatches", "Accept:application/vnd.github.everest-preview+json", "event_type=demo"]
}
