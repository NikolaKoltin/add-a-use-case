// UC-STU-deactivate-student: the control, its confirmation, and the result. The deactivation API is stubbed,
// so these tests check what the dialog does with each answer and what the control renders; the backend's own
// tests cover who may deactivate whom and the extensions (403/409).
// Run against the dev stack (backend + `npm run dev`) with `npm run cy:open`.

const activeStudent = {
  id: 5,
  username: 'e.hudson@abc.edu',
  firstName: 'Eric',
  lastName: 'Hudson',
  email: 'e.hudson@abc.edu',
  enabled: true,
  roles: 'student',
  sectionId: 2,
  sectionName: '2023-2024',
  teamId: 1,
  teamName: 'Team1'
}

const deactivatedStudent = { ...activeStudent, id: 6, firstName: 'Woody', lastName: 'Allen', enabled: false }

function studentsPage(content) {
  return {
    flag: true,
    code: 200,
    message: 'Find students successfully',
    data: {
      content,
      number: 0,
      size: 50,
      totalElements: content.length,
      totalPages: 1,
      last: true,
      first: true,
      empty: content.length === 0
    }
  }
}

describe('Deactivate a student', () => {
  beforeEach(() => {
    cy.login('b.gates@abc.edu', '123456')
    cy.intercept('POST', '**/students/search*', studentsPage([activeStudent, deactivatedStudent])).as('students')
    cy.visit('/students')
    cy.wait('@students')
  })

  it('shows the Deactivate control only for a student whose account is active (3a)', () => {
    cy.contains('.el-table__row', 'Hudson').find('[data-cy=deactivate-student]').should('exist')
    cy.contains('.el-table__row', 'Allen').find('[data-cy=deactivate-student]').should('not.exist')
  })

  it('sends no request when the instructor cancels at the confirmation (5a)', () => {
    cy.intercept('POST', '**/students/5/deactivation').as('deactivate')
    cy.contains('.el-table__row', 'Hudson').find('[data-cy=deactivate-student]').click()

    cy.get('.el-message-box').contains('button', 'Cancel').click()

    cy.get('@deactivate.all').should('have.length', 0)
  })

  it('warns of the consequences and asks for confirmation (step 4)', () => {
    cy.contains('.el-table__row', 'Hudson').find('[data-cy=deactivate-student]').click()

    cy.get('.el-message-box')
      .should('contain', 'Eric Hudson')
      .and('contain', 'no longer be able to log in')
      .and('contain', 'kept')
    cy.get('.el-message-box').contains('button', 'Cancel').click() // leave no dialog open behind
  })

  it('deactivates the student on confirmation, reloads the list, and hides the control for that row (step 6)', () => {
    cy.intercept('POST', '**/students/5/deactivation', {
      flag: true,
      code: 200,
      message: 'Deactivate student successfully',
      data: { ...activeStudent, enabled: false }
    }).as('deactivate')
    cy.intercept('POST', '**/students/search*', studentsPage([{ ...activeStudent, enabled: false }, deactivatedStudent])).as('reloaded')

    cy.contains('.el-table__row', 'Hudson').find('[data-cy=deactivate-student]').click()
    cy.get('.el-message-box').contains('button', 'Deactivate').click()

    cy.wait('@deactivate')
    cy.contains('.el-message', 'Eric Hudson has been deactivated').should('be.visible')
    cy.wait('@reloaded')
    cy.contains('.el-table__row', 'Hudson').find('[data-cy=deactivate-student]').should('not.exist')
  })
})
